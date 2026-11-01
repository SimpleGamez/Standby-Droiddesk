# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.standby.mode.**$$serializer { *; }
-keepclassmembers class com.standby.mode.** {
    *** Companion;
}
-keepclasseswithmembers class com.standby.mode.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Room entities
-keep class com.standby.mode.data.model.** { *; }
