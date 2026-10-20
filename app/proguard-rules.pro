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
