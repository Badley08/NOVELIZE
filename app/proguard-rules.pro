# ProGuard and R8 configuration for NoveLize
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
-keep class com.karlitodev.novelize.ui.releasenotes.** { *; }

# ---------------------------------------------------------------------------
# PdfBox-Android — optional runtime dependencies not present on Android
# com.gemalto.jp2.JP2Decoder is used only when decoding JPX-compressed PDF
# images. The class is never available on stock Android; suppress the R8
# missing-class error so the build succeeds without affecting functionality.
# ---------------------------------------------------------------------------
-dontwarn com.gemalto.jp2.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.pdfbox.**
-dontwarn com.tom_roush.pdfbox.filter.JPXFilter
-dontwarn org.apache.fontbox.**

# ONNX Runtime — native JNI bridge; keep the reflection-accessed entry points
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# ML Kit — keep public API surface used via reflection
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
