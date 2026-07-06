# dietie

Native Android rewrite of the uploaded Dietrix PWA, built with Kotlin + Jetpack Compose and a Material 3 Expressive-inspired UI.

## What is included

- App name: **dietie**
- Package: `com.dietie.app`
- Native Kotlin/Compose implementation, not a WebView
- Persian RTL layout
- Light/dark mode toggle
- IBW/AIBW calculation logic copied from the uploaded web app
- Maintenance calories and target calories
- Editable macro percentages with a donut chart
- Food unit totals
- Meal plan cards
- Food unit guide
- Formula explainer
- Local calculation history with delete/clear actions
- Original uploaded PWA is preserved in `original-web/`

## Open in Android Studio

1. Open Android Studio.
2. Choose **Open** and select this `dietie_android` folder.
3. Let Gradle sync.
4. Run the `app` configuration on an emulator or device.

## Notes

The project uses Compose Material 3 alpha APIs for `MaterialExpressiveTheme`:

```kotlin
implementation("androidx.compose.material3:material3:1.5.0-alpha23")
```

If Gradle sync fails because your Android Studio/Gradle setup is older, update Android Studio and install Android SDK 36, or downgrade AGP/compileSdk while keeping the Compose code intact.
