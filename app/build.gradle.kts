plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.swift_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.swift_app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.viewpager2)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.core.splashscreen)
    implementation(libs.swiperefreshlayout)
    implementation(libs.gridlayout)


    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // UI / Animations
    implementation(libs.lottie)
    implementation(libs.mpandroidchart)
    implementation(libs.zxing)
    implementation(libs.zxing.android)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}