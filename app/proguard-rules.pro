# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin serialization & annotations
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keep,includedescriptorclasses class com.standby.mode.**$$serializer { *; }
-keepclassmembers class com.standby.mode.** {
    *** Companion;
}
-keepclasseswithmembers class com.standby.mode.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Domain models & Data entities
-keep class com.standby.mode.domain.model.** { *; }
-keep class com.standby.mode.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Ktor CIO
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
