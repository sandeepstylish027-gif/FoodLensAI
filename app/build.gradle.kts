plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.foodlens.ai"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.foodlens.ai"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    // ============================================================
    // TFLITE MODEL ASSETS
    // ============================================================

    androidResources {
        noCompress += "tflite"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    externalNativeBuild {
        cmake {
            path = file(
                "src/main/cpp/CMakeLists.txt"
            )
        }
    }
}

dependencies {

    // ============================================================
    // ANDROIDX
    // ============================================================

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.activity.compose
    )

    // ============================================================
    // ONNX RUNTIME
    // ============================================================

    implementation(
        "com.microsoft.onnxruntime:onnxruntime-android:1.20.0"
    )

    // ============================================================
    // TENSORFLOW LITE
    // ============================================================

    implementation(
        "org.tensorflow:tensorflow-lite:2.17.0"
    )

    // ============================================================
    // LITERT
    // ============================================================

    implementation(
        "com.google.ai.edge.litert:litert:1.4.2"
    )

    // ============================================================
    // JETPACK COMPOSE
    // ============================================================

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )

    // ============================================================
    // UNIT TESTS
    // ============================================================

    testImplementation(
        libs.junit
    )

    // ============================================================
    // ANDROID TESTS
    // ============================================================

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    // ============================================================
    // DEBUG
    // ============================================================

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
}