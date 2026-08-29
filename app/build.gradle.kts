plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

import java.io.File
import java.security.MessageDigest

val prepareLauncherIcon by tasks.registering {
    val source = rootProject.file("docs/branding/trailcharter-logo-primary.png")
    val destination = layout.projectDirectory.file("src/main/res/drawable/trailcharter_logo_primary.png")
    inputs.file(source)
    outputs.file(destination)
    doLast {
        check(source.isFile) { "Missing launcher icon source: $source" }
        destination.asFile.parentFile.mkdirs()
        source.copyTo(destination.asFile, overwrite = true)
    }
}

android {
    namespace = "com.masamunr.trailcharter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.masamunr.trailcharter"
        minSdk = 28
        targetSdk = 36
        versionCode = 11
        versionName = "0.2.3-alpha1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("continuityDebug") {
            val keystorePath = providers.environmentVariable("TRAILCHARTER_CI_KEYSTORE_PATH").orNull
            val keystorePassword = providers.environmentVariable("TRAILCHARTER_CI_KEYSTORE_PASSWORD").orNull
            val keyAliasValue = providers.environmentVariable("TRAILCHARTER_CI_KEY_ALIAS").orNull
            val keyPasswordValue = providers.environmentVariable("TRAILCHARTER_CI_KEY_PASSWORD").orNull
            if (!keystorePath.isNullOrBlank() && !keystorePassword.isNullOrBlank() && !keyAliasValue.isNullOrBlank() && !keyPasswordValue.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".mapspike"
            versionNameSuffix = "-mapspike"
            val continuityPath = providers.environmentVariable("TRAILCHARTER_CI_KEYSTORE_PATH").orNull
            if (!continuityPath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("continuityDebug")
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

// Keep the normal TrailCharter application identity/version at versionCode 11. Only the isolated
// debug .mapspike APK advances for physical spike installs; path visual hierarchy test is version 23.
androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        variant.outputs.forEach { output ->
            output.versionCode.set(23)
        }
    }
}

tasks.named("preBuild").configure { dependsOn(prepareLauncherIcon) }

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin { jvmToolchain(17) }

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.maplibre.android)
    implementation(libs.protomaps.pmtiles)
    implementation(project(":brouter-core"))
    implementation(project(":brouter-expressions"))
    implementation(project(":brouter-mapaccess"))
    implementation(project(":brouter-util"))
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
