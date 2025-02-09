import java.util.Properties

//// Чтение данных из local.properties
val localProperties = rootProject.file("local.properties")
val googleBooksApiKey: String = Properties().apply {
    localProperties.inputStream().use { load(it) }
}.getProperty("GOOGLE_BOOKS_API_KEY") ?: throw IllegalStateException("API Key not found")


plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.favbook"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.favbook"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Используем правильную форму для добавления custom BuildConfig поля
        buildConfigField("String", "GOOGLE_BOOKS_API_KEY", "\"$googleBooksApiKey\"")
    }

    buildFeatures {
        buildConfig = true // Эта строка включит поддержку кастомных полей в BuildConfig
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    //УБРАТЬ ВСЕ В libs.versions
    implementation ("com.squareup.okhttp3:okhttp:4.10.0") // или более новая версия
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0") // зависимость для логирования
    implementation ("androidx.navigation:navigation-compose:2.8.6")
    implementation ("androidx.compose.material:material-icons-core:1.7.7")
    implementation ("androidx.compose.material:material-icons-extended:1.7.7") // для расширенных иконок
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}