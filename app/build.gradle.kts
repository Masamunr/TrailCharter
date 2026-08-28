import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val launcherArtworkSource = layout.projectDirectory.file("branding/ic_launcher.webp.b64")
val generatedLauncherResDir = layout.buildDirectory.dir("generated/trailcharterLauncher/res")

val prepareLauncherIcon by tasks.registering {
    inputs.file(launcherArtworkSource)
    outputs.dir(generatedLauncherResDir)

    doLast {
        val encoded = launcherArtworkSource.asFile.readText().trim()
        val artwork = Base64.getDecoder().decode(encoded)
        val mipmapDir = generatedLauncherResDir.get().dir("mipmap-xxxhdpi").asFile
        mipmapDir.mkdirs()
        mipmapDir.resolve("ic_launcher.webp").writeBytes(artwork)
        mipmapDir.resolve("ic_launcher_foreground.webp").writeBytes(artwork)
    }
}

android {
    namespace = "com.masamunr.trailcharter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.masamunr.trailcharter"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1-foundation"
    }

    sourceSets {
        getByName("main").res.srcDir(generatedLauncherResDir)
    }

    buildTypes {
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

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(prepareLauncherIcon)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
