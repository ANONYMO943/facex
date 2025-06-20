
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.facex"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.facex"
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
    implementation("com.google.mlkit:face-detection:16.1.5")

    implementation("com.google.firebase:firebase-ml-vision:24.1.0") {
        // Apply exclude rules to this specific dependency
        exclude(group = "com.google.android.gms", module = "play-services-vision")
        exclude(group = "com.google.android.gms", module = "play-services-vision-common")
    }

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
apply(plugin = "com.google.gms.google-services")
