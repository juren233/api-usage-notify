# UsageNotify ProGuard rules

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Room entities
-keep class com.juren233.usagenotify.data.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Google Tink / Security-Crypto (missing errorprone annotations)
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
