plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "hk.kirk.trustme"
    compileSdk = 36

    defaultConfig {
        applicationId = "hk.kirk.trustme"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "1.0.5"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "trustme-release.jks"
            storeFile = file(keystorePath)
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "trustme123"
            keyAlias = System.getenv("KEY_ALIAS") ?: "trustme"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "trustme123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
    }

    // Apache HTTP legacy 库支持
    useLibrary("org.apache.http.legacy")
}

// Rename APK output: TrustMe-v1.0.3-release.apk
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                val buildType = variant.buildType ?: "debug"
                val version = variant.outputs.first().versionName.get()
                output.outputFileName.set("TrustMe-v${version}-${buildType}.apk")
            }
        }
    }
}

dependencies {
    // Xposed API — compileOnly，不打包进 APK
    compileOnly("de.robv.android.xposed:api:82")

    // Apache HTTP 组件
    implementation("org.apache.httpcomponents:httpcore:4.4.16")

    // Jetpack Compose (2026.05.01 BOM)
    val composeBom = platform("androidx.compose:compose-bom:2026.05.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.activity:activity-compose:1.13.0")
}
