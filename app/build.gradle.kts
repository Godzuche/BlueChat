plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.bluechat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bluechat"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0-rc01")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui:1.7.0-alpha08")
    implementation("androidx.compose.ui:ui-graphics:1.7.0-alpha08")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.0-alpha08")
    implementation("androidx.compose.material3:material3:1.3.0-alpha06")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.0-alpha08")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.0-alpha08")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.0-alpha08")

    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.51.1")
    ksp ("com.google.dagger:hilt-android-compiler:2.51.1")
    ksp ("androidx.hilt:hilt-compiler:1.2.0")

    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.8.0-rc01")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.4")

}