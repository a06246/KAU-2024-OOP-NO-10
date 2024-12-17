// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0") // Gradle 플러그인 버전
        classpath("com.google.gms:google-services:4.3.15") // Google 서비스 클래스패스 추가

    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
