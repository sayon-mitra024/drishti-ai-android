# Third-Party Notices

Drishti AI Android is built on open-source software and public research resources. These remain under their own licenses and terms. Nothing in this repository's [LICENSE](LICENSE) changes or overrides them.

> Licenses below are listed to the best of the author's knowledge. Always confirm against each project's current license before redistributing anything.

## Software Libraries

| Component | Use in this project | License |
|-----------|--------------------|---------|
| Kotlin and Kotlin Coroutines | Language and async | Apache License 2.0 |
| AndroidX / Jetpack Compose | UI framework | Apache License 2.0 |
| Material Components / Material 3 | Theming | Apache License 2.0 |
| CameraX | Camera capture | Apache License 2.0 |
| Room and DataStore | Local storage | Apache License 2.0 |
| Coil | Image loading | Apache License 2.0 |
| ONNX Runtime (Android) | On-device inference | MIT License |

## Planned Components (not yet integrated)

| Component | Intended use | Terms |
|-----------|-------------|-------|
| Firebase Android SDK | Cloud synchronization | Apache License 2.0 for the SDK; use of Firebase services is subject to the Firebase and Google Cloud terms |
| DID / cryptography libraries | Decentralized identity and record security | To be listed when selected |

## Model and Research Resources

| Resource | Notes |
|----------|-------|
| EfficientNet architecture (Tan and Le, 2019) | Published research architecture |
| PyTorch and torchvision | Used for model development and export; BSD-style licenses |
| ImageNet normalization statistics | Standard mean and standard deviation values used in preprocessing |
| APTOS 2019 Blindness Detection dataset | Used in research experiments only. Not distributed here. Subject to the publisher's competition and data-use terms. Verify these before any commercial or redistributed use. |

## Design and Assets

Fonts, icons and other visual assets, if any, are used under their own licenses. The Drishti AI logo is part of the project's protected materials (see [OWNERSHIP.md](OWNERSHIP.md)).

## Trademarks

Android, Google, Firebase, Kaggle, Hugging Face, ONNX, PyTorch and other names are trademarks of their respective owners. Their use here is descriptive only and does not imply endorsement.

## Updating This File

When a dependency is added, removed or changed, update this file in the same commit. For a complete, machine-generated list, the Gradle license report can be produced with a plugin such as `com.jaredsburrows.license`.
