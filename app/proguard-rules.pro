# ProGuard and R8 configuration for NoveLize
# Aggressive shrinking and optimization settings

# Strip line numbers and source file names to reduce DEX size
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Strip Android logging statements from release builds to optimize binary size and remove debug traces
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Standard optimizations
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

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
