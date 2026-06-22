# Keep domain model classes used by Room
-keep class com.shreyash.sensorapp.domain.model.** { *; }
-keep class com.shreyash.sensorapp.data.local.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-dontwarn androidx.compose.**
