# AGENTS.md — SensorApp

Android sensor monitoring app. Kotlin + Compose (Material 3), Clean Architecture, Hilt, Room, Flow.

## Commands
- `./gradlew :app:assembleDebug` — debug APK
- `./gradlew :app:compileDebugKotlin` — Kotlin-only compile check
- `./gradlew :app:lintDebug` — lint
- `./gradlew :app:testDebugUnitTest` — unit tests

## Architecture
- `domain/` — zero Android imports. Pure Kotlin models, interfaces, use cases.
- `data/` — Room entities/DAOs, `SensorDataSource` (callbackFlow), `SensorRepositoryImpl`.
- `presentation/` — Compose screens, ViewModels, theme, navigation, permission handler.
- `di/` — Hilt modules (AppModule, DatabaseModule, SensorModule).

## Key rules
- UI never touches DAO or SensorManager — always through `SensorRepository`.
- Permissions JIT only (never at launch). Step Counter → `ACTIVITY_RECOGNITION` on API 29+.
- Unavailable sensors shown greyed out → bottom sheet explanation. Never hidden.
- Sensor flows lifecycle-aware: `callbackFlow`, unregistered on `ON_STOP`, resumed on `ON_START`.
- Lint: remove unused imports on every edit. Build with `compileDebugKotlin` before commit.
- Dependencies in `gradle/libs.versions.toml` only.

## Screens
| Screen | Layout | Key detail |
|--------|--------|------------|
| Dashboard | 2-col `LazyVerticalGrid` | Category headers, card tiles with icon circles, live values |
| Detail | Scrollable | Gradient chart, crosshair + tooltip on tap, Start/Stop Logging, CSV export |
| History | `LazyColumn` | Session-based cards (not individual readings), search bar, sort, clear dialog |
| Settings | Scrollable | Polling rate (radio), DB stats, credits (Shreyash Pattewar) |

## Quirks
- Chart uses in-memory buffer (last 60 readings), not Room.
- Each Start/Stop Logging creates/ends a `LogSession` row in Room. `onCleared()` ends active session.
- Polling delay stored in-memory only (`currentDelay`). No DataStore.
- CSV export: `MediaStore.Downloads` (API 29+), `Environment` fallback (pre-Q).
- DB v2 with `fallbackToDestructiveMigration()` (early dev).
- 10 sensors supported at runtime.
