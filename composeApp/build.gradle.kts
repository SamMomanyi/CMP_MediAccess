import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.buildKonfig)
}

// Load the properties file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val firebaseApiKey = localProperties.getProperty("FIREBASE_API_KEY") ?: ""
val firebaseProjectId = localProperties.getProperty("FIREBASE_PROJECT_ID") ?: ""

buildkonfig {
    // This must match your app's package name
    packageName = "org.sammomanyi.mediaccess"

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "FIREBASE_API_KEY",
            localProperties.getProperty("FIREBASE_API_KEY") ?: ""
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "FIREBASE_PROJECT_ID",
            localProperties.getProperty("FIREBASE_PROJECT_ID") ?: ""
        )
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "GOOGLE_WEB_CLIENT_ID", project.findProperty("GOOGLE_WEB_CLIENT_ID")?.toString() ?: "")
    }
}
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    jvm("desktop")

            // This is the fix: explicitly 'getting' the desktopMain source set

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
//koin
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.ktor.client.okhttp)
            // ... google dependencies ...
            implementation(libs.google.services.auth)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.googleid)


            // ... accompanist dependency...
            implementation(libs.accompanist.flowlayout)
            // --- Compass Location ---
            implementation(libs.compass.geolocation)
            implementation(libs.compass.geolocation.mobile)
            implementation(libs.compass.permissions.mobile)

            // --- Compass Geocoding ---
            implementation(libs.compass.geocoder)
            implementation(libs.compass.geocoder.mobile)

            // --- Compass Autocomplete ---
            implementation(libs.compass.autocomplete)
            implementation(libs.compass.autocomplete.mobile)

            // ...coil dependecies ...
            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp) // ← THIS is what was missing
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Ktor & Serialization
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)

            // CRITICAL: Room KMP Runtime must be here for imports to work
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.androidx.datastore.preferences)
            //koin
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.core)

            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)
            implementation(libs.kotlinx.datetime)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")    // Make sure this exists!
            implementation(libs.compose.icons.extended)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Image Loading (For the News Images)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            implementation(libs.jetbrains.compose.navigation)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
        }


        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        // Room Compiler (KSP) must be in this top-level block

    }
}
dependencies {
    // Room KMP requires the compiler on every platform target
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}

// Room Configuration for KMP
room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "org.sammomanyi.mediaccess"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.sammomanyi.mediaccess"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)

    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.animation.android)
    implementation(libs.androidx.benchmark.traceprocessor.android)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.animation.core.android)

}

compose.desktop {
    application {
        mainClass = "org.sammomanyi.mediaccess.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.sammomanyi.mediaccess"
            packageVersion = "1.0.0"
        }
    }
}
