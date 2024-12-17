plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Firebase 서비스를 위한 Gradle 플러그인
}

android {
    namespace = "com.example.accountbooks"
    compileSdk = 35 // 최신 Android SDK 사용을 위해 compileSdk를 35로 설정

    defaultConfig {
        applicationId = "com.example.accountbooks"
        minSdk = 24 // 최소 SDK 버전 설정
        targetSdk = 34 // 타겟 SDK 버전 설정
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

    buildFeatures {
        viewBinding = true // ViewBinding 활성화
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Java 17 호환성을 위해 설정
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17" // Kotlin에서 JVM 17 사용
    }
}

dependencies {
    implementation("androidx.fragment:fragment-ktx:1.6.1") // Fragment 사용을 위한 KTX 라이브러리
    implementation("androidx.activity:activity-ktx:1.8.2") // Activity 사용을 위한 KTX 라이브러리
    implementation("androidx.core:core-ktx:1.12.0") // 기본 Android KTX 라이브러리

    // MPAndroidChart 라이브러리 (버전 형식 수정, 기존 v3.1.0 → 3.1.0으로 변경)
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0") // 수정된 부분: 잘못된 버전 형식 수정

    implementation(libs.androidx.appcompat) // AppCompat 지원 라이브러리
    implementation(libs.material) // Material 디자인 라이브러리
    implementation(libs.androidx.activity) // Activity 지원 라이브러리
    implementation(libs.androidx.constraintlayout) // ConstraintLayout 사용을 위한 라이브러리

    // Firebase 관련 라이브러리
    implementation(libs.firebase.auth.ktx) // Firebase Authentication 사용
    implementation(libs.firebase.firestore.ktx) // Firebase Firestore 사용
    implementation(libs.firebase.database) // Firebase Realtime Database 사용

    // 테스트 의존성 추가
    testImplementation(libs.junit) // JUnit 테스트 라이브러리
    androidTestImplementation(libs.androidx.junit) // AndroidX JUnit 테스트 라이브러리
    androidTestImplementation(libs.androidx.espresso.core) // Espresso UI 테스트 라이브러리

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
}

// 저장소 설정 (추가된 부분)
// JitPack을 사용하여 MPAndroidChart 라이브러리를 다운로드할 수 있도록 설정
repositories {
    google() // Google 저장소
    mavenCentral() // Maven 중앙 저장소
    maven("https://jitpack.io") // 수정된 부분: JitPack 저장소 추가
}
