// プロジェクトルートの build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//        maven {
//            url = uri("https://jitpack.io")
//        }
//        maven {
//            url = uri("https://maven.google.com")
//        }
//    }
//    dependencies {
//        classpath("com.android.tools.build:gradle:7.4.2")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
//    }
//}

