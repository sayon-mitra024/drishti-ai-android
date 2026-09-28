package com.example.engine

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Locale
import kotlin.math.exp

/**
 * Result data class for Drishti diabetic retinopathy prediction.
 */
data class DrishtiPrediction(
    val classIndex: Int,
    val className: String,
    val confidence: Float,
    val probabilities: Map<Int, Float>,
    val noDrProbability: Float = probabilities[0] ?: 0f,
    val mildProbability: Float = probabilities[1] ?: 0f,
    val moderateProbability: Float = probabilities[2] ?: 0f,
    val severeProbability: Float = probabilities[3] ?: 0f,
    val proliferativeDrProbability: Float = probabilities[4] ?: 0f,
    val camBitmap: Bitmap? = null
)

/**
 * Offline AI classifier for Diabetic Retinopathy screening using ONNX Runtime.
 *
 * Model: EfficientNet-B0 (drishti_model.onnx)
 * Input shape: [1, 3, 224, 224] Float32 (RGB, bilinear resize, ImageNet normalized)
 * Output: 5-class logits mapped via softmax to ICDR DR severity stages.
 */
class DrishtiClassifier(
    private val context: Context,
    private val modelAssetPath: String = "drishti_model.onnx",
    private val labelsAssetPath: String = "labels.json"
) : Closeable {

    companion object {
        private const val TAG = "DrishtiClassifier"

        const val INPUT_WIDTH = 224
        const val INPUT_HEIGHT = 224
        const val INPUT_CHANNELS = 3
        const val NUM_CLASSES = 5

        // ImageNet normalization statistics
        val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        val STD = floatArrayOf(0.229f, 0.224f, 0.225f)

        val DEFAULT_LABELS = mapOf(
            0 to "No DR",
            1 to "Mild",
            2 to "Moderate",
            3 to "Severe",
            4 to "Proliferative DR"
        )
    }

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var inputName: String? = null
    private var labels: Map<Int, String> = DEFAULT_LABELS
    private var isInitialized = false

    private var classifierWeights: FloatArray? = null

    /**
     * Initializes the ONNX environment, loads the model from assets, and loads label definitions.
     */
    @Synchronized
    fun initialize(): Result<Unit> {
        if (isInitialized && ortSession != null) {
            return Result.success(Unit)
        }

        return try {
            // Load label definitions from assets
            loadLabels()

            // Preload classifier weights for CAM computation
            loadClassifierWeights()

            // Initialize ONNX Runtime environment
            val env = OrtEnvironment.getEnvironment()
            ortEnvironment = env

            // Load model byte array from assets
            val modelBytes = context.assets.open(modelAssetPath).use { it.readBytes() }
            if (modelBytes.isEmpty()) {
                throw IllegalStateException("Model asset '$modelAssetPath' is empty or could not be read.")
            }

            val sessionOptions = OrtSession.SessionOptions()
            val session = env.createSession(modelBytes, sessionOptions)
            ortSession = session

            // Identify model input name dynamically
            inputName = session.inputNames.iterator().next()
            isInitialized = true
            Log.i(TAG, "Drishti ONNX Runtime initialized successfully. Input: $inputName, Output: ${session.outputNames}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize DrishtiClassifier", e)
            Result.failure(e)
        }
    }

    /**
     * Loads 5x1280 classification weights for Class Activation Map (CAM) generation.
     * First attempts loading from assets/classifier_weights.bin, then falls back to extracting
     * directly from the drishti_model.onnx initializers.
     */
    private fun loadClassifierWeights(): FloatArray? {
        classifierWeights?.let { return it }

        // 1. Try loading pre-extracted classifier_weights.bin
        try {
            context.assets.open("classifier_weights.bin").use { input ->
                val bytes = input.readBytes()
                if (bytes.size >= CamGenerator.NUM_CLASSES * CamGenerator.FEATURE_CHANNELS * 4) {
                    val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                    val fb = byteBuffer.asFloatBuffer()
                    val weights = FloatArray(CamGenerator.NUM_CLASSES * CamGenerator.FEATURE_CHANNELS)
                    fb.get(weights)
                    classifierWeights = weights
                    Log.i(TAG, "Loaded ${weights.size} classifier weights from classifier_weights.bin")
                    return weights
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load classifier_weights.bin, attempting fallback: ${e.message}")
        }

        // 2. Direct fallback: parse classifier.1.weight initializer directly from ONNX model
        try {
            context.assets.open(modelAssetPath).use { input ->
                val buf = input.readBytes()
                val target = "classifier.1.weight".toByteArray(Charsets.US_ASCII)
                val pos = indexOfSubarray(buf, target, 100000)
                if (pos != -1) {
                    val dataStart = pos + target.size + 4 // skip tag and protobuf length varint
                    val totalBytes = CamGenerator.NUM_CLASSES * CamGenerator.FEATURE_CHANNELS * 4
                    if (dataStart + totalBytes <= buf.size) {
                        val byteBuffer = ByteBuffer.wrap(buf, dataStart, totalBytes).order(ByteOrder.LITTLE_ENDIAN)
                        val fb = byteBuffer.asFloatBuffer()
                        val weights = FloatArray(CamGenerator.NUM_CLASSES * CamGenerator.FEATURE_CHANNELS)
                        fb.get(weights)
                        classifierWeights = weights
                        Log.i(TAG, "Extracted classifier weights directly from ONNX model initializers")
                        return weights
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback extraction of classifier weights failed", e)
        }

        return null
    }

    private fun indexOfSubarray(array: ByteArray, target: ByteArray, startIndex: Int = 0): Int {
        if (target.isEmpty() || array.size < target.size) return -1
        for (i in startIndex..(array.size - target.size)) {
            var match = true
            for (j in target.indices) {
                if (array[i + j] != target[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }

    /**
     * Loads labels from assets/labels.json, falling back to DEFAULT_LABELS on failure.
     */
    private fun loadLabels() {
        try {
            val jsonString = context.assets.open(labelsAssetPath).bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val parsedLabels = mutableMapOf<Int, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val index = key.toIntOrNull()
                if (index != null) {
                    parsedLabels[index] = jsonObject.getString(key)
                }
            }
            if (parsedLabels.isNotEmpty()) {
                labels = parsedLabels
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load $labelsAssetPath, using default labels: ${e.message}")
            labels = DEFAULT_LABELS
        }
    }

    /**
     * Runs offline inference on the provided Bitmap.
     * Executes asynchronously on Dispatchers.Default to avoid blocking the UI thread.
     */
    suspend fun classify(bitmap: Bitmap): Result<DrishtiPrediction> = withContext(Dispatchers.Default) {
        try {
            if (!isInitialized || ortSession == null) {
                val initResult = initialize()
                if (initResult.isFailure) {
                    return@withContext Result.failure(
                        initResult.exceptionOrNull() ?: IllegalStateException("Initialization failed")
                    )
                }
            }

            val env = ortEnvironment ?: throw IllegalStateException("ONNX environment is null")
            val session = ortSession ?: throw IllegalStateException("ONNX session is null")
            val currentInputName = inputName ?: session.inputNames.iterator().next()

            // 1. Bilinear resize to 224x224 (no center crop, no pad, filter = true for bilinear interpolation)
            val scaledBitmap = if (bitmap.width == INPUT_WIDTH && bitmap.height == INPUT_HEIGHT) {
                bitmap
            } else {
                Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
            }

            // 2. Extract RGB pixels and construct CHW FloatBuffer with ImageNet normalization
            val floatBuffer = preprocessImageToChw(scaledBitmap)

            // 3. Create input tensor [1, 3, 224, 224]
            val shape = longArrayOf(1, INPUT_CHANNELS.toLong(), INPUT_HEIGHT.toLong(), INPUT_WIDTH.toLong())
            val inputTensor = OnnxTensor.createTensor(env, floatBuffer, shape)

            // 4. Run inference locally using ONNX Runtime
            val output = inputTensor.use { tensor ->
                session.run(mapOf(currentInputName to tensor))
            }

            // 5. Read outputs: logits and cam_features
            output.use { result ->
                if (result.size() == 0) {
                    throw IllegalStateException("Model returned empty output")
                }

                // Locate logits output (either named "logits" or fallback to first output)
                var logitsOnnxValue: Any? = null
                var camFeaturesOnnxValue: ai.onnxruntime.OnnxValue? = null

                for (entry in result) {
                    when (entry.key) {
                        "logits" -> logitsOnnxValue = entry.value.value
                        "cam_features" -> camFeaturesOnnxValue = entry.value
                    }
                }

                if (logitsOnnxValue == null) {
                    logitsOnnxValue = result.get(0).value
                }

                val logits = extractLogits(logitsOnnxValue)

                // 6. Apply softmax exactly once to obtain probabilities
                val probabilities = applySoftmax(logits)

                // 7. Find highest-probability class
                var maxIdx = 0
                var maxProb = probabilities[0]
                for (i in 1 until probabilities.size) {
                    if (probabilities[i] > maxProb) {
                        maxProb = probabilities[i]
                        maxIdx = i
                    }
                }

                val probMap = probabilities.mapIndexed { index, prob -> index to prob }.toMap()
                val className = labels[maxIdx] ?: "Unknown (Class $maxIdx)"

                // 8. Generate mathematical Class Activation Map (CAM) using true model features & weights
                var camBitmap: Bitmap? = null
                val features = extractFloatArray(camFeaturesOnnxValue)
                val weights = loadClassifierWeights()

                if (features != null && weights != null) {
                    camBitmap = CamGenerator.computeCamBitmap(
                        features = features,
                        classifierWeights = weights,
                        classIndex = maxIdx,
                        targetWidth = bitmap.width,
                        targetHeight = bitmap.height
                    )
                }

                Result.success(
                    DrishtiPrediction(
                        classIndex = maxIdx,
                        className = className,
                        confidence = maxProb,
                        probabilities = probMap,
                        camBitmap = camBitmap
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Inference error in DrishtiClassifier", e)
            Result.failure(e)
        }
    }

    /**
     * Converts a 224x224 Bitmap into a direct FloatBuffer formatted as CHW with ImageNet normalization.
     */
    private fun preprocessImageToChw(bitmap: Bitmap): FloatBuffer {
        val totalPixels = INPUT_WIDTH * INPUT_HEIGHT
        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

        // Direct ByteBuffer allocated for native JNI transfer
        val byteBuffer = ByteBuffer.allocateDirect(1 * INPUT_CHANNELS * totalPixels * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer()

        val rMean = MEAN[0]
        val gMean = MEAN[1]
        val bMean = MEAN[2]

        val rStd = STD[0]
        val gStd = STD[1]
        val bStd = STD[2]

        val channelStride = totalPixels
        val floatArray = FloatArray(INPUT_CHANNELS * channelStride)

        val rOffset = 0
        val gOffset = channelStride
        val bOffset = 2 * channelStride

        for (i in 0 until totalPixels) {
            val color = pixels[i]
            val r = ((color shr 16) and 0xFF) / 255.0f
            val g = ((color shr 8) and 0xFF) / 255.0f
            val b = (color and 0xFF) / 255.0f

            floatArray[rOffset + i] = (r - rMean) / rStd
            floatArray[gOffset + i] = (g - gMean) / gStd
            floatArray[bOffset + i] = (b - bMean) / bStd
        }

        floatBuffer.put(floatArray)
        floatBuffer.rewind()
        return floatBuffer
    }

    /**
     * Extracts logits array from the generic ONNX output.
     */
    private fun extractLogits(rawValue: Any?): FloatArray {
        return when (rawValue) {
            is Array<*> -> {
                when (val firstElement = rawValue[0]) {
                    is FloatArray -> firstElement
                    is Array<*> -> (firstElement as Array<Float>).toFloatArray()
                    else -> throw IllegalStateException("Unexpected 2D array component: ${firstElement?.javaClass}")
                }
            }
            is FloatArray -> rawValue
            else -> throw IllegalStateException("Unsupported output tensor type: ${rawValue?.javaClass}")
        }
    }

    /**
     * Applies numerically stable Softmax to logits array.
     */
    private fun applySoftmax(logits: FloatArray): FloatArray {
        if (logits.isEmpty()) return floatArrayOf()
        var maxVal = logits[0]
        for (i in 1 until logits.size) {
            if (logits[i] > maxVal) {
                maxVal = logits[i]
            }
        }

        val expValues = FloatArray(logits.size)
        var sumExp = 0.0f
        for (i in logits.indices) {
            val v = exp(logits[i] - maxVal)
            expValues[i] = v
            sumExp += v
        }

        return if (sumExp > 0.0f) {
            FloatArray(logits.size) { i -> expValues[i] / sumExp }
        } else {
            FloatArray(logits.size) { 1.0f / logits.size }
        }
    }

    /**
     * Debug and verification method that loads a Bitmap, runs inference, and prints formatted output.
     */
    suspend fun testAndPrint(bitmap: Bitmap): DrishtiPrediction? = withContext(Dispatchers.Default) {
        val result = classify(bitmap)
        result.onSuccess { pred ->
            println("Predicted class: ${pred.className}")
            println(String.format(Locale.US, "Confidence: %.4f", pred.confidence))
            println(String.format(Locale.US, "No DR: %.4f", pred.probabilities[0] ?: 0f))
            println(String.format(Locale.US, "Mild: %.4f", pred.probabilities[1] ?: 0f))
            println(String.format(Locale.US, "Moderate: %.4f", pred.probabilities[2] ?: 0f))
            println(String.format(Locale.US, "Severe: %.4f", pred.probabilities[3] ?: 0f))
            println(String.format(Locale.US, "Proliferative DR: %.4f", pred.probabilities[4] ?: 0f))
        }.onFailure { error ->
            System.err.println("DrishtiClassifier verification error: ${error.message}")
        }
        result.getOrNull()
    }

    /**
     * Extracts float array from an OnnxTensor representing intermediate convolutional features.
     */
    private fun extractFloatArray(onnxValue: ai.onnxruntime.OnnxValue?): FloatArray? {
        if (onnxValue == null) return null
        return try {
            if (onnxValue is OnnxTensor) {
                val fb = onnxValue.floatBuffer.duplicate()
                fb.rewind()
                val arr = FloatArray(fb.remaining())
                fb.get(arr)
                arr
            } else {
                null
            }
        } catch (e: Exception) {
            try {
                when (val raw = onnxValue.value) {
                    is FloatArray -> raw
                    is Array<*> -> flattenFloatArray(raw)
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun flattenFloatArray(arr: Array<*>): FloatArray {
        val list = ArrayList<Float>(CamGenerator.FEATURE_CHANNELS * CamGenerator.SPATIAL_SIZE)
        fun recurse(item: Any?) {
            when (item) {
                is FloatArray -> for (f in item) list.add(f)
                is Array<*> -> for (sub in item) recurse(sub)
            }
        }
        recurse(arr)
        return list.toFloatArray()
    }

    override fun close() {
        try {
            ortSession?.close()
        } catch (_: Exception) {}
        try {
            ortEnvironment?.close()
        } catch (_: Exception) {}
        ortSession = null
        ortEnvironment = null
        classifierWeights = null
        isInitialized = false
    }
}
