plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleHilt)
    alias(libs.plugins.googleKsp)
    alias(libs.plugins.googleGms)
    alias(libs.plugins.googleCrashlytics)
}

android {
    namespace = "com.yangdai.opennote"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yangdai.opennote"
        minSdk = 29
        targetSdk = 34
        versionCode = 110
        versionName = "1.1.0"
        resourceConfigurations += listOf("en", "zh")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)

    // CommonMark, for markdown rendering and parsing
    implementation(libs.commonmark.ext.task.list.items)
    implementation(libs.commonmark.ext.gfm.strikethrough)
    implementation(libs.commonmark.ext.gfm.tables)
    implementation(libs.commonmark.ext.heading.anchor)
    implementation(libs.commonmark.ext.image.attributes)
    implementation(libs.commonmark)

    // CameraX
    implementation (libs.androidx.camera.core)
    implementation (libs.androidx.camera.camera2)
    implementation (libs.androidx.camera.lifecycle)
    implementation (libs.androidx.camera.view)

    // To recognize Latin script
    implementation (libs.text.recognition)
    // To recognize Chinese script
    implementation (libs.text.recognition.chinese)

    // Firebase, if u don't have google-services.json, remove this block
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Room, for local database
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)

    // Hilt, for dependency injection
    implementation(libs.androidx.hilt)
    ksp(libs.google.hilt.compiler)
    implementation(libs.google.hilt)

    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.material.icons)
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)
    implementation (libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
