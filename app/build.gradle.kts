plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.gcc.gccapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gcc.gccapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
        buildConfigField("String", "BASE_URL","\"https://gcc-application-be.vercel.app/api/\"")
//        buildConfigField("String", "BASE_URL","\"http://192.168.1.13:3000/\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL","\"https://gcc-application-be.vercel.app/api/\"")
        }
        debug {
            buildConfigField("String", "BASE_URL","\"https://gcc-application-be.vercel.app/api/\"")
        }

    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    //Firebase Cloude Messaging
    implementation("com.google.firebase:firebase-messaging-ktx:23.2.1")

    // Glide
    implementation(libs.github.glide)

    // Ucrop
    implementation(libs.yalantis.ucrop)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.android.material:material:1.9.0")
    implementation ("com.google.android.gms:play-services-base:18.5.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")

}