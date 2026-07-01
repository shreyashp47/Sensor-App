# SensorApp

Android Kotlin app (minSdk 24, targetSdk 36, v1.7). Package: `com.shreyash.sensorapp`.

## Tech
Kotlin 1.9.22 · Jetpack Compose (BOM 2024.02.00) · Material 3 · Hilt 2.50 · Room 2.6.1 · Coroutines 1.7.3 · Compose Navigation 2.7.7

## Architecture
Clean Architecture: data/domain/presentation layers with Dagger Hilt DI.

## Build
`./gradlew assembleDebug` — requires JDK 17+ (use Android Studio JBR at `/Applications/Android Studio.app/Contents/jbr/Contents/Home`).

## Sensors (10)
Accelerometer, Gyroscope, Linear Acceleration, Magnetometer, Gravity, Rotation Vector, Light, Proximity, Barometer, Step Counter.

## Screens
- **Dashboard** — 2-col grid grouped by category (Motion/Position/Environmental/Activity). Compass card at top.
- **Compass** — tilt-compensated heading via accel+mag fusion with Canvas-drawn rose.
- **Sensor Detail** — live values, Canvas chart (60 readings), logging toggle, CSV export.
- **History** — session list with search, sort, clear.
- **Settings** — polling rate (FASTEST/GAME/UI/NORMAL), DB stats, credits.

## Key Patterns
- Sensor observation via `callbackFlow` in `SensorDataSource`, scaled by polling delay from Settings.
- `combine` in CompassViewModel fuses accel+mag flows using `SensorManager.getRotationMatrix()`.
- Navigation via sealed `Route` class in `Routes.kt`.
- Previews use extracted `*ScreenContent` composables with mock data (no Hilt).
- Dark theme only.
