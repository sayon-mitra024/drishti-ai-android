# Ownership and Attribution

This document explains who owns what in the **Drishti AI Android** repository, what is protected, what belongs to others, and how the work may be used and cited. It should be read together with [LICENSE](LICENSE) and [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).

## 1. Rights Holder

**Sayon Mitra** — B.Tech CSE Core, Chandigarh University
Copyright © 2026 Sayon Mitra. All rights reserved, except where otherwise stated.

- Permissions and licensing: sayon@sayonedu.in
- General contact: sayonmitracode@gmail.com

## 2. Project Context

Drishti AI is a Smart India Hackathon (SIH) 2026 project by Team Wave, addressing *Explainable AI for Diabetic Retinopathy Screening in Rural India*. This repository contains the native Android client. It also serves as a research artifact and as supporting material for a thesis presentation.

| Repository | Description |
|------------|-------------|
| [`drishti-ai-android`](https://github.com/sayon-mitra024/drishti-ai-android) | This repository — offline-first Android client |
| [`drishti-ai`](https://github.com/sayon-mitra024/drishti-ai) | Companion web application |

## 3. Protected Original Materials

The following are original works of Sayon Mitra and are covered by the [LICENSE](LICENSE):

- The Android application source code (Kotlin, Jetpack Compose UI, resources and build configuration written for this project)
- The application architecture, workflow design and on-device inference pipeline as implemented here
- The on-device image-quality assessment logic and CAM generation code
- The local data model and storage design
- Written documentation in this repository (README and related files)
- The Drishti AI name, logo and project-specific visual identity, as used by this project
- The bundled or referenced trained model checkpoint (`efficientnet_b0_dr.onnx`), to the extent it is the author's own trained work, and not any third-party component it derives from

## 4. Materials Not Owned by the Author

This repository does not claim ownership of, and does not relicense, the following. Each remains under its own license, terms or rights holder:

- **Third-party libraries and frameworks** — Kotlin, AndroidX (Jetpack Compose, CameraX, Room, DataStore), Material components, ONNX Runtime, Coil, Kotlin Coroutines, and any others used. See [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
- **Model architecture and pretrained foundations** — the EfficientNet-B0 architecture and PyTorch/torchvision tooling, and any pretrained initialization weights (for example ImageNet-pretrained weights) used before fine-tuning.
- **Datasets** — the APTOS 2019 Blindness Detection dataset and any other dataset used in research. Datasets are not distributed here and remain subject to their own terms, including the competition and data-use rules of their publishers.
- **External services** — Firebase, Google services, Hugging Face and similar services, as used or planned. Their terms of service apply to any use of them.
- **Fonts, icons and assets** created by others.
- **Team, institutional and organizational contributions** — where other people, Team Wave members, Chandigarh University, the SIH organizers or the problem-statement organization have contributed material or hold rights in it, those contributions are governed by the applicable agreements and are not claimed here.

## 5. Model and Data Notice

- The model weights are part of the protected work. The public repository does not authorize reuse of the model, retraining on it, or redistribution of it.
- Training data and evaluation metrics for the deployed checkpoint are not documented in this repository.
- No patient data, clinical records or personally identifiable information belong in this repository. If you find any, please report it to sayon@sayonedu.in.
- Any future cloud storage of screening records (Firebase) and identity data (DID) will be governed by the privacy documentation published with those features.

## 6. Permitted Uses Without Further Permission

You may, without asking:

- View and read the repository, and fork it within GitHub as GitHub's Terms of Service permit
- Reference and discuss the work in academic, review or evaluation settings
- Quote short excerpts of the documentation with clear attribution, as fair use or fair dealing allows
- Cite the project as shown below

## 7. Uses That Require Written Permission

Please contact sayon@sayonedu.in **before** you:

- Copy, modify, or build on the source code, outside of the GitHub actions described above
- Redistribute the code, application (APK/AAB) or any bundled model
- Publish a derived application or use the Drishti AI name or logo
- Use the work for commercial purposes, or in any product or service
- Use the work in a clinical, diagnostic or patient-facing setting

## 8. Medical Use Notice

Drishti AI Android is a research prototype and is **not a medical device**. Ownership of this repository does not imply clinical validation, regulatory approval or endorsement by any medical authority. See the Medical Disclaimer in the [README](README.md).

## 9. How to Cite

```
Mitra, S. (2026). Drishti AI Android: Offline-first explainable AI-assisted
diabetic retinopathy screening on Android [Software]. Team Wave, Smart India
Hackathon 2026. https://github.com/sayon-mitra024/drishti-ai-android
```

## 10. Corrections

If you believe any material here is attributed incorrectly, or infringes your rights, contact sayon@sayonedu.in with the details and it will be reviewed and corrected promptly.
