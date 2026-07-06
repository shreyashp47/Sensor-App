# SensorApp

Android Kotlin app (minSdk 24, targetSdk 36). Package: `com.shreyash.sensorapp`.

## Tech Stack
- **Kotlin** 1.9.22 · **Jetpack Compose** (BOM 2024.02.00) · **Material 3**
- **Hilt** 2.50 · **Room** 2.6.1 · **Coroutines** 1.7.3 · **Compose Navigation** 2.7.7

## Build
```bash
./gradlew assembleDebug
```
Requires JDK 17+ (use Android Studio JBR at `/Applications/Android Studio.app/Contents/jbr/Contents/Home`).

CI: `.github/workflows/build.yml` — lint on PR, bump+signed AAB+upload+commit on merge to `main`.

---

## Architecture — Clean Architecture

```
data/       ← SensorDataSource (callbackFlow), Room DB, RepositoryImpl, HapticManager
domain/     ← Models (SensorType, SensorReading), Repository interface, UseCases
presentation/ ← Screens, ViewModels, Navigation, Theme
di/         ← Hilt modules (SensorModule, DatabaseModule)
```

---

## Sensors (10)

| Sensor | Type | Category | Axes | Units |
|--------|------|----------|------|-------|
| Accelerometer | Hardware | Motion | 3 | m/s² |
| Gyroscope | Hardware | Motion | 3 | °/s |
| Linear Acceleration | Virtual | Motion | 3 | m/s² |
| Magnetometer | Hardware | Position | 3 | µT |
| Gravity | Virtual | Position | 3 | m/s² |
| Rotation Vector | Virtual | Position | 3 | — |
| Light | Hardware | Environmental | 1 | lx |
| Proximity | Hardware | Environmental | 1 | binary (OBSTRUCTED/CLEAR) |
| Barometer | Hardware | Environmental | 1 | hPa |
| Step Counter | Hardware | Activity | 1 | steps |

---

## Screens

### Dashboard
- 2-column `LazyVerticalGrid`, grouped by `SensorCategory`
- `CompassDashboardCard` at top spanning full width
- Each `SensorGridItem` shows icon, name, availability
- Unavailable sensors show `ModalBottomSheet` on tap
- Permission dialog for `ACTIVITY_RECOGNITION` (step counter)
- **No real-time observation** (removed for perf — was causing lag)

### Compass
- Rotating dial (iPhone-style), fixed red indicator at top
- Degree labels (30/60/120/150/…) drawn radially around dial
- Tilt-compensated heading via `SensorManager.getRotationMatrix()`
- Small disclaimer: "Accuracy depends on device calibration"
- Dark canvas, no Card/LIVE indicator

### Sensor Detail Screens (10 individual files)
Each sensor has its own screen composable in `presentation/detail/`:

| File | Sensor | Unique UI |
|------|--------|-----------|
| `AccelerometerScreen.kt` | Accelerometer | X/Y/Z values + chart |
| `GyroscopeScreen.kt` | Gyroscope | X/Y/Z values + **3D cube** + chart |
| `LinearAccelerationScreen.kt` | Linear Acceleration | X/Y/Z values + chart |
| `MagnetometerScreen.kt` | Magnetometer | X/Y/Z values + chart |
| `GravityScreen.kt` | Gravity | X/Y/Z values + chart |
| `RotationVectorScreen.kt` | Rotation Vector | X/Y/Z values + chart |
| `LightScreen.kt` | Light | Brightness level card (DARK→SUNLIGHT) + gradient bar + chart |
| `ProximityScreen.kt` | Proximity | OBSTRUCTED/CLEAR card + chart |
| `PressureScreen.kt` | Barometer | Weather condition card (STORM/RAIN/NORMAL/HIGH) + chart |
| `StepCounterScreen.kt` | Step Counter | Step count + chart |

All screens share:
- `SensorDetailScaffold` — top bar, lifecycle binding, bottom bar with **Logging toggle** + **CSV Export**
- `LiveLineChart` — Canvas line chart (60 readings), touch crosshair with tooltip
- `LiveIndicator` — animated pulsing green dot
- `SensorUsageHint` — contextual usage tip per sensor type
- `SensorDetailScaffold` applies content padding (fixed: was hiding behind toolbar)

### History
- Session list from Room DB, search bar, sort toggle (newest/oldest)
- Clear all button with confirmation

### Settings
- **Polling Rate** — RadioGroup: FASTEST / GAME / UI / NORMAL
- **Haptic Feedback** — Switch toggle (on by default), controls vibration for proximity/step/gyro
- **Database Stats** — total row count
- **Credits** — developer info

---

## Key Shared Components (`presentation/detail/`)

| File | What it exports |
|------|-----------------|
| `SensorDetailScaffold.kt` | `SensorDetailScaffold()` — shared scaffold + lifecycle + CSV export |
| `SensorUtils.kt` | `LiveIndicator()`, `SensorUsageHint()`, `formatLargeValue()`, `formatDetailValue()` |
| `SensorChart.kt` | `LiveLineChart()` — Canvas chart with touch crosshair |
| `SensorDisplayCard.kt` | `LiveValueDisplay()`, `AxisValue()`, `PressureDisplay()`, `LightDisplay()`, `ProximityDisplay()` |
| `GyroscopeCube.kt` | `GyroscopeCube()` — 3D rotating cube on Canvas |
| `DetailViewModel.kt` | `DetailViewModel` — sensor observation, 60-reading buffer, logging, **haptic triggers** |

---

## Haptic Feedback (#14)

Implemented in `DetailViewModel.checkHapticTriggers()`:

| Sensor | Trigger | Haptic |
|--------|---------|--------|
| Proximity | OBSTRUCTED ↔ CLEAR transition | `doubleTick()` |
| Step Counter | Step count increments | `tick()` |
| Gyroscope | Rotation magnitude > 5 rad/s | `tick()` |

`HapticManager` (`data/sensor/HapticManager.kt`) wraps `Vibrator`/`VibratorManager`, compatible with API 24+. Toggle in Settings (stored in-memory via `@Volatile` in `SensorRepositoryImpl`).

**Requires `android.permission.VIBRATE`** in manifest (normal permission, granted at install time).

---

## Navigation

`Routes.kt` — sealed `Route` class with per-sensor routes:
```
dashboard, history, settings, compass
sensor/accelerometer, sensor/gyroscope, sensor/linear_acceleration, sensor/magnetometer
sensor/gravity, sensor/rotation_vector, sensor/light, sensor/proximity
sensor/pressure, sensor/step_counter
```

`SensorType.toRoute()` extension maps each enum to its route string. `AppNavGraph.kt` registers individual composable destinations. No nav arguments needed — each screen passes sensor type directly to `SensorDetailScaffold`.

---

## Data Flow

```
SensorManager callbackFlow
  → SensorDataSource.observeSensor(type, delay)
    → SensorRepositoryImpl.observeSensor(type)
      → ObserveSensorUseCase(sensorType)
        → DetailViewModel (collect → buffer 60 → check haptics)
          → Screen composable (collectAsStateWithLifecycle)
```

Polling delay from `SensorRepositoryImpl.currentDelay` (`@Volatile`, defaults to `SENSOR_DELAY_UI`). Haptic toggle from `SensorRepositoryImpl.hapticEnabled`.

---

## Key Patterns
- Sensor observation via `callbackFlow` in `SensorDataSource`
- DI via Hilt (`@HiltViewModel`, `@Inject`, `@Module`)
- Dark theme only (no light/auto switch yet)
- Previews use `*Content` composables with mock data (no Hilt)
- No DataStore/SharedPreferences — all settings are in-memory `@Volatile` vars (lost on app restart)
