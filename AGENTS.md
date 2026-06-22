# AGENTS.md — SensorApp

Android sensor monitoring app. Kotlin, Jetpack Compose (Material 3), Clean Architecture, Hilt, Room, Coroutines + Flow.

## Dev commands

| Command | What it does |
|---------|-------------|
| `./gradlew :app:assembleDebug` | Build debug APK |
| `./gradlew :app:lintDebug` | Run lint |
| `./gradlew :app:testDebugUnitTest` | Run unit tests |
| `./gradlew connectedCheck` | Instrumented tests on device |

Dependencies via `gradle/libs.versions.toml` — update there, not in individual build files.

## Architecture constraints

- **Domain layer (`domain/`)** — zero Android/platform imports. Pure Kotlin.
- **Repository** — UI never touches DAO or SensorManager. Always through `SensorRepository` interface.
- **ViewModels** — all business logic in `domain/usecase/`. Composables are thin.
- **Sensor flows** — `callbackFlow` in `SensorDataSource`. Listener lifecycle tied to `DisposableEffect(LifecycleOwner)` — unregistered on `ON_STOP`, re-registered on `ON_START`.

## Key structure

```
com.example.sensorapp/
├── data/          Room entities, SensorDao, AppDatabase, SensorDataSource, RepositoryImpl
├── domain/        Models, repository interface, UseCases (no Android deps)
├── presentation/  Screens, ViewModels, Navigation, Theme, PermissionHandler
└── di/            Hilt modules (AppModule, DatabaseModule, SensorModule)
```

10 sensors supported at runtime: Accelerometer, Gyroscope, Magnetometer, Light, Proximity, Barometer, Step Counter, Gravity, Linear Acceleration, Rotation Vector.

## Quirks & gotchas

- **Permissions never requested at launch.** Only JIT on user action (Step Counter → `ACTIVITY_RECOGNITION` on API 29+). `PermissionHandler.kt` handles the full dialog → request → denials → Open Settings flow.
- **Unavailable sensors** always shown as greyed-out cards with "Not available" chip → bottom sheet explanation. Never hidden or crashed.
- **Logging toggle** is in-memory only (no persistence). Settings reset on process death.
- **Polling delay** stored in-memory (`SensorRepositoryImpl.currentDelay`). No DataStore/SharedPreferences.
- **CSV export** uses `MediaStore.Downloads` (API 29+) with fallback to `Environment.getExternalStoragePublicDirectory` (pre-Q).
- **Chart** is Canvas-drawn (no Vico dependency). Shows last 60 readings from Room, auto-scales Y.
- **Live dot** pulsing via `rememberInfiniteTransition().animateFloat()`.

## Testing notes

- Inject `SensorDataSource` with a fake `SensorManager` for unit tests.
- Domain layer testable in isolation — no Android dependencies.
- `SensorDao` can be tested with Room in-memory database (`Room.inMemoryDatabaseBuilder`).
