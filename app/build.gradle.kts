plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tera.weather_open_meteo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tera.weather_open_meteo"
        minSdk = 26
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(files("libs/ProgressAnim.aar"))
    implementation(files("libs/CheckBox.aar"))

    //implementation(files("libs/Chart106.aar"))
    implementation("io.github.uratera:chart:1.0.6")

    // SharedPreferences (для предпочтений)
    implementation ("androidx.preference:preference-ktx:1.2.1")
    // OpenStreetMap (для функции карты)
    implementation ("org.osmdroid:osmdroid-android:6.1.20")

    implementation("com.github.skydoves:balloon:1.6.12")

    implementation(libs.volley)
    implementation(libs.gson)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}