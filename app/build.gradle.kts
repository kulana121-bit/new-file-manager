plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ravi.newfilemanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ravi.newfilemanager"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        // Release keystore comes from GitHub Secrets (see .github/workflows/build.yml).
        // KEYSTORE_FILE points at a PKCS12 file restored from KEYSTORE_BASE64.
        val keystoreFileEnv = System.getenv("KEYSTORE_FILE")
        val keystorePasswordEnv = System.getenv("KEYSTORE_PASSWORD")
        val keyAliasEnv = System.getenv("KEY_ALIAS")
        val keyPasswordEnv = System.getenv("KEY_PASSWORD")

        val hasReleaseCredentials = !keystoreFileEnv.isNullOrBlank() &&
                !keystorePasswordEnv.isNullOrBlank() &&
                !keyAliasEnv.isNullOrBlank() &&
                !keyPasswordEnv.isNullOrBlank()

        if (hasReleaseCredentials) {
            val keyFile = file(keystoreFileEnv)
            if (!keyFile.exists()) {
                throw org.gradle.api.GradleException("Release KEYSTORE_FILE specified at '$keystoreFileEnv' does not exist!")
            }
            create("release") {
                storeFile = keyFile
                storeType = "PKCS12"
                storePassword = keystorePasswordEnv
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            }
        }
    }

    buildTypes {
        debug {
            // debug builds stay unoptimized for speed
        }
        release {
            // R8: proven recipe from the old Files app (release-81-1).
            // minify ON, no obfuscation, no aggressive optimization, keep all classes.
            // Resource shrinking stays OFF (it stripped resources and crashed the old app).
            isMinifyEnabled = true
            isShrinkResources = false
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning != null) {
                signingConfig = releaseSigning
            } else {
                // Never silently ship a release build with the wrong signature.
                signingConfig = null
            }
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.documentfile:documentfile:1.0.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
