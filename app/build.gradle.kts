plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin") // Safe Args eklendi
    id("kotlin-android")
    id("kotlin-kapt") // Kotlin annotation processing'i etkinleştirin
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk =34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    // Glide'ı ekliyoruz
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.androidx.media3.common.ktx)  // Glide'ın son sürümünü buraya yazın
    kapt ("com.github.bumptech.glide:compiler:4.15.1")// Bu satır sadece Glide için gerekli
    implementation (libs.androidx.appcompat.v120)

    implementation(libs.androidx.fragment.ktx)
    annotationProcessor(libs.compiler)



    implementation(libs.androidx.navigation.fragment.ktx.v250) // Versiyon kontrol edin

    implementation(libs.material.v1120)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat.v170)
    implementation(libs.androidx.navigation.fragment.ktx.v253)
    implementation(libs.androidx.navigation.ui.ktx.v253)

    implementation(libs.picasso)
    implementation(libs.androidx.recyclerview)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth.v2310)
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.androidx.drawerlayout)

    // AndroidX Core Kütüphaneleri
    implementation(libs.androidx.core.ktx.v1150)
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation(libs.androidx.activity.ktx.v172)

    // Firebase Firestore ve Storage
    implementation("com.google.firebase:firebase-firestore-ktx:24.7.1")
    implementation(libs.firebase.storage.ktx.v2020)

    // Test Kütüphaneleri
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
