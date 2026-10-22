# ProGuard and R8 configuration for NoveLize v2.0
# Aggressive shrinking and optimization settings

# Keep attributes required for reflection, serialization, and debugging
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Strip Android logging statements from release builds to optimize binary size
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Standard optimizations (do NOT use -repackageclasses which breaks class loaders and serialization)
-allowaccessmodification

# Keep kotlinx.serialization generated serializers and companion objects
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    <fields>;
}

# Keep Navigation 3 NavKey destinations
-keep class com.karlitodev.novelize.** implements androidx.navigation3.runtime.NavKey { *; }
-keep interface androidx.navigation3.runtime.NavKey { *; }
-keep class * extends androidx.navigation3.runtime.NavKey { *; }

# Keep data models, preferences, and crash telemetry
-keep class com.karlitodev.novelize.model.** { *; }
-keep class com.karlitodev.novelize.data.** { *; }
-keep class com.karlitodev.novelize.crash.** { *; }
