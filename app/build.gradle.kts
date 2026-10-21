plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.karlitodev.novelize"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.karlitodev.novelize"
        minSdk = 28
        targetSdk = 37
        maxSdk = 37
        versionCode = 11
        versionName = "1.1"
        // Permanent unique identifier for this application build, used for analytics
        // and crash report correlation. Generated once and never changes between releases.
        buildConfigField("String", "APP_UID", "\"89a2c264f3574a618d3393ba79c3ec8c\"")
    }

    // Build a single universal APK that installs on all ABI architectures.
    // This avoids the split-apk install failure seen on devices that do not
    // receive a matching split artifact.
    splits {
        abi {
            isEnable = true
            reset()
            // Include all supported architectures in one universal APK
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    // Signing credentials are read from environment variables only.
    // The keystore file is listed in .gitignore and must never be committed.
    // CI injects these via GitHub Actions secrets.
    // Local developers must export the variables before building:
    //   export KEYSTORE_PATH=/path/to/novelize-release.jks
    //   export KEYSTORE_STORE_PASS=...
    //   export KEYSTORE_KEY_ALIAS=...
    //   export KEYSTORE_KEY_PASS=...
    val envKeystorePath = System.getenv("KEYSTORE_PATH")
    val envStorePass    = System.getenv("KEYSTORE_STORE_PASS")
    val envKeyAlias     = System.getenv("KEYSTORE_KEY_ALIAS")
    val envKeyPass      = System.getenv("KEYSTORE_KEY_PASS")

    signingConfigs {
        if (envKeystorePath != null && envStorePass != null && envKeyAlias != null && envKeyPass != null) {
            create("release") {
                storeFile     = file(envKeystorePath)
                storePassword = envStorePass
                keyAlias      = envKeyAlias
                keyPassword   = envKeyPass
            }
        }
    }

    buildTypes {
        release {
            // Wire the release signing config when credentials are available
            val releaseSigningConfig = signingConfigs.findByName("release")
            if (releaseSigningConfig != null) {
                signingConfig = releaseSigningConfig
            }
            // Enable R8 code shrinking and obfuscation
            isMinifyEnabled = true
            // Enable resource shrinking to remove unused assets and resources
            isShrinkResources = true
            // Ensure debuggable mode is disabled
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Disable debuggable mode per project configuration
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      // Enabled to expose APP_UID and version constants via BuildConfig at runtime
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Material Icons extended set for rich in-app iconography
  implementation("androidx.compose.material:material-icons-extended")
  // Physics-based animation (spring, decay, fling)
  implementation("androidx.compose.animation:animation")
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // ONNX Runtime — executes Piper neural voice model ONNX checkpoints on-device
  // without any internet connection or cloud inference.
  implementation(libs.onnx.runtime.android)

  // ML Kit Entity Extraction — on-device NLP model that identifies entities
  // (names, places, dates) in chapter text to generate comprehension quiz questions.
  implementation(libs.mlkit.entity.extraction)

  // ML Kit Language Identification — detects whether a loaded book is EN or FR
  // to route narration to the correct Piper voice model.
  implementation(libs.mlkit.language.id)
}
