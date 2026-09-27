package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.engine.ImageQualityChecker
import com.example.model.DiabeticRetinopathyGrade
import com.example.model.QualityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Drishti AI", appName)
    }

    @Test
    fun `test ICDR grades enumeration`() {
        assertEquals(5, DiabeticRetinopathyGrade.entries.size)
        assertEquals("Grade 0 (Normal)", DiabeticRetinopathyGrade.GRADE_0.shortName)
        assertEquals(false, DiabeticRetinopathyGrade.GRADE_0.isReferable)
        assertEquals(true, DiabeticRetinopathyGrade.GRADE_2.isReferable)
        assertEquals(true, DiabeticRetinopathyGrade.GRADE_4.isReferable)
    }

    @Test
    fun `test image quality checker on blank image`() {
        val testBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        val result = ImageQualityChecker.evaluate(testBitmap)
        assertNotNull(result)
        // A blank black bitmap will fail illumination / suitability
        assertEquals(QualityStatus.FAIL, result.overallStatus)
    }
}
