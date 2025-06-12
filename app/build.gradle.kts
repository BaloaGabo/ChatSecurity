import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.googleService)
}

android {
    namespace = "com.example.chatdocuemysi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chatdocuemysi"
        minSdk = 24
        targetSdk = 35
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
        compose = true
    }

    // ←<<<<<<< Aquí agregas esto PARA RESOLVER LOS META-INF DUPLICADOS
    // Excluye las dos entradas duplicadas
    fun Packaging.() {
        resources {
            // Excluye las dos entradas duplicadas
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)// Componentes de Material Design 3 para Compose

    // Dependencias de Firebase
    implementation(libs.firebase.auth.ktx) // Autenticación de Firebase (Kotlin)
    implementation(libs.firebase.database.ktx) // Realtime Database de Firebase (Kotlin)
    implementation(libs.firebase.storage.ktx) // Cloud Storage de Firebase (Kotlin)
    implementation(libs.firebase.messaging) // Firebase Cloud Messaging (FCM) para notificaciones
    implementation(libs.firebase.appcheck.ktx) // App Check para proteger el backend de Firebase

    // Autenticación de Google y WorkManager
    implementation(libs.play.services.auth.v2100) // Servicios de Google Play para autenticación
    implementation(libs.androidx.work.runtime.ktx) // WorkManager para tareas en segundo plano (Kotlin)

    // Navegación y Permisos
    implementation(libs.androidx.navigation.compose) // Navegación en Jetpack Compose
    implementation(libs.google.accompanist.permissions) // Librería de Accompanist para gestionar permisos

    // Librerías de UI y procesamiento de imágenes
    implementation(libs.circleImage) // Posiblemente para imágenes circulares o avatares
    implementation(libs.glide) // Librería para carga y caché de imágenes (aunque Coil también está presente, podría haber redundancia o uso específico)
    implementation(libs.coil.compose) // Carga de imágenes moderna para Compose
    implementation(libs.coil.network.okhttp) // Integración de Coil con OkHttp para red
    implementation (libs.photoView) // Librería para zoom y pan en imágenes (visor de fotos)
    implementation(libs.androidx.icons.extended) // Iconos extendidos de Material Design para Compose
    implementation (libs.androidx.ui.text.google.fonts) // Integración de fuentes de Google Fonts en Compose

    implementation(libs.volley) // Librería para peticiones HTTP

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

apply(plugin = "com.google.gms.google-services")