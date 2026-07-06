# dietie

Native Android app built with Kotlin, Jetpack Compose and Material 3 styling.

## Build without Android Studio

This version intentionally uses stable build tooling:

- Android Gradle Plugin 8.10.1
- Gradle 8.11.1
- Kotlin 2.1.21
- compileSdk 36
- Compose BOM 2024.10.00

The UI keeps the expressive Material 3 look with custom color scheme, large rounded shapes, cards, chips and motion, but avoids the experimental Material 3 Expressive alpha dependency that required API 37 and caused GitHub Actions SDK failures.

Upload the project to GitHub, then run **Actions → Build dietie APK → Run workflow**. The debug APK will appear as the `dietie-debug-apk` artifact.
