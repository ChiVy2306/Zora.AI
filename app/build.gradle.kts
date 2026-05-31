plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.yozora.aichat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yozora.aichat"
        minSdk = 26
        targetSdk = 35
        versionCode = 102
        versionName = "1.0.2"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "brand"
    productFlavors {
        create("zora") {
            dimension = "brand"
            resValue("string", "app_name", "Zora.AI")
            resValue("string", "app_version_panel", "Zora.AI v1.0.2")
        }
        create("slv") {
            dimension = "brand"
            applicationIdSuffix = ".slv"
            resValue("string", "app_name", "SanLoVerse (SLV)")
            resValue("string", "app_version_panel", "SanLoVerse (SLV) v1.0.2")
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

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
