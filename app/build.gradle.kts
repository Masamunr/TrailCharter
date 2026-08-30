import java.security.MessageDigest

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val launcherArtworkSource = layout.projectDirectory.file("branding/ic_launcher_shield.webp")
val generatedLauncherResDir = layout.buildDirectory.dir("generated/trailcharterLauncher/res")
val expectedLauncherArtworkSha256 = "881ca2eca089dcd7ee89f0ed1dd425cd6cd8fddb4480526a6571928c450f3fb8"

val ciSigningStorePath = providers.environmentVariable("TRAILCHARTER_CI_KEYSTORE_PATH").orNull
val ciSigningStorePassword = providers.environmentVariable("TRAILCHARTER_CI_KEYSTORE_PASSWORD").orNull
val ciSigningKeyAlias = providers.environmentVariable("TRAILCHARTER_CI_KEY_ALIAS").orNull
val ciSigningKeyPassword = providers.environmentVariable("TRAILCHARTER_CI_KEY_PASSWORD").orNull
val ciSigningConfigured = listOf(ciSigningStorePath, ciSigningStorePassword, ciSigningKeyAlias, ciSigningKeyPassword).all { !it.isNullOrBlank() }

val prepareLauncherIcon by tasks.registering {
    inputs.file(launcherArtworkSource)
    outputs.dir(generatedLauncherResDir)

    doLast {
        val artwork = launcherArtworkSource.asFile.readBytes()
        check(artwork.size >= 12) { "Launcher artwork is too small to be a valid WebP file" }
        check(String(artwork, 0, 4, Charsets.US_ASCII) == "RIFF") { "Launcher artwork is not a RIFF file" }
        check(String(artwork, 8, 4, Charsets.US_ASCII) == "WEBP") { "Launcher artwork is not a WebP file" }
        val declaredRiffSize = (artwork[4].toInt() and 0xff) or ((artwork[5].toInt() and 0xff) shl 8) or ((artwork[6].toInt() and 0xff) shl 16) or ((artwork[7].toInt() and 0xff) shl 24)
        check(declaredRiffSize + 8 == artwork.size) { "Launcher artwork is truncated or has unexpected trailing bytes" }
        val actualSha256 = MessageDigest.getInstance("SHA-256").digest(artwork).joinToString("") { "%02x".format(it.toInt() and 0xff) }
        check(actualSha256 == expectedLauncherArtworkSha256) { "Launcher artwork bytes do not match the approved shield source" }
        val mipmapDir = generatedLauncherResDir.get().dir("mipmap-xxxhdpi").asFile
        mipmapDir.mkdirs()
        mipmapDir.resolve("ic_launcher.webp").writeBytes(artwork)
        mipmapDir.resolve("ic_launcher_round.webp").writeBytes(artwork)
        mipmapDir.resolve("ic_launcher_artwork.webp").writeBytes(artwork)
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
    }

    sourceSets { getByName("main").res.srcDir(generatedLauncherResDir) }

    signingConfigs {
        if (ciSigningConfigured) {
            create("continuityDebug") {
                storeFile = file(requireNotNull(ciSigningStorePath))
                storePassword = requireNotNull(ciSigningStorePassword)
                keyAlias = requireNotNull(ciSigningKeyAlias)
                keyPassword = requireNotNull(ciSigningKeyPassword)
            }
        }
    }

    buildTypes {
        debug {
            // This draft branch is a technical spike. Keep it installable beside the real
            // TrailCharter alpha so physical renderer/routing testing cannot touch Adventure data.
            applicationIdSuffix = ".mapspike"
            versionNameSuffix = "-mapspike"
            if (ciSigningConfigured) signingConfig = signingConfigs.getByName("continuityDebug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
// debug .mapspike APK advances for physical spike installs; Stage-route persistence is version 25.
androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        variant.outputs.forEach { output ->
            output.versionCode.set(25)
        }
    }
}

tasks.named("preBuild").configure { dependsOn(prepareLauncherIcon) }

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.maplibre.compose) {
        exclude(group = "org.maplibre.gl", module = "android-sdk")
    }
    implementation(libs.maplibre.android.opengl)

    // Technical routing spike only. BRouter's public core API exposes classes from modules that it
    // declares internally as Gradle implementation dependencies, so make all five source modules
    // visible to TrailCharter while keeping the whole set pinned to the same immutable v1.7.10 tag.
    implementation("org.btools:brouter-core:v1.7.10")
    implementation("org.btools:brouter-mapaccess:v1.7.10")
    implementation("org.btools:brouter-util:v1.7.10")
    implementation("org.btools:brouter-expressions:v1.7.10")
    implementation("org.btools:brouter-codec:v1.7.10")

    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
