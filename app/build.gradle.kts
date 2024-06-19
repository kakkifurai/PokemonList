plugins {
    id("com.android.application")
    id("kotlin-android")
    //id("com.gradle.plugin-publish") version "0.17.0"

}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // RxJava3 用の Retrofit アダプター
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    // RxJava3 コルーチンアダプター
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // RxJava3 ライブラリ
    implementation("io.reactivex.rxjava3:rxjava:3.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.firebase:firebase-analytics:21.0.0")

}
