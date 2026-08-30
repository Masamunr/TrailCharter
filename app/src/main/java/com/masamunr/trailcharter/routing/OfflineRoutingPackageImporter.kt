package com.masamunr.trailcharter.routing

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipFile
import org.json.JSONObject

internal data class InstalledOfflineRoutingPackage(
    val segmentDirectory: File,
    val profile: File,
    val lookups: File,
)

private const val ROUTING_PACKAGE_SCHEMA_VERSION = 1
private const val ROUTING_PACKAGE_ID = "uk-wales-eryri-brouter-walk-spike"
private const val ROUTING_ENGINE_NAME = "BRouter"
private const val ROUTING_ENGINE_VERSION = "1.7.10"
private const val ROUTING_MANIFEST_PATH = "manifest.json"
private const val ROUTING_SEGMENT_PATH = "segments4/W5_N50.rd5"
private const val ROUTING_PROFILE_PATH = "profiles2/hiking-mountain.brf"
private const val ROUTING_LOOKUPS_PATH = "profiles2/lookups.dat"

private const val MAX_ROUTING_ARCHIVE_BYTES = 190L * 1024L * 1024L
private const val MAX_ROUTING_MANIFEST_BYTES = 1024L * 1024L
private const val MAX_ROUTING_SEGMENT_BYTES = 180L * 1024L * 1024L
private const val MAX_ROUTING_PROFILE_BYTES = 1024L * 1024L
private const val MAX_ROUTING_LOOKUPS_BYTES = 8L * 1024L * 1024L

private data class ExpectedRoutingPayload(
    val key: String,
    val path: String,
    val maxBytes: Long,
)

private data class ValidatedRoutingPayload(
    val expected: ExpectedRoutingPayload,
    val bytes: Long,
    val sha256: String,
)

private val expectedRoutingPayloads = listOf(
    ExpectedRoutingPayload("segment", ROUTING_SEGMENT_PATH, MAX_ROUTING_SEGMENT_BYTES),
    ExpectedRoutingPayload("profile", ROUTING_PROFILE_PATH, MAX_ROUTING_PROFILE_BYTES),
    ExpectedRoutingPayload("lookups", ROUTING_LOOKUPS_PATH, MAX_ROUTING_LOOKUPS_BYTES),
)

internal fun loadInstalledEryriRoutingPackage(context: Context): InstalledOfflineRoutingPackage? =
    loadRoutingPackageFromDirectory(installedRoutingPackageDirectory(context))

/**
 * Imports a routing package selected through Android's document picker into private app storage.
 *
 * Validation happens in staging first. A bad or interrupted import therefore cannot deliberately
 * replace the currently installed routing package with half-copied data.
 */
internal fun importEryriRoutingPackage(context: Context, source: Uri): InstalledOfflineRoutingPackage {
    val temporaryArchive = context.cacheDir.resolve("routing-import-${UUID.randomUUID()}.zip")
    val root = routingPackageRoot(context).also { it.mkdirs() }
    val staging = root.resolve(".staging-${UUID.randomUUID()}")
    val backup = root.resolve(".backup-${UUID.randomUUID()}")
    val installed = installedRoutingPackageDirectory(context)

    try {
        copySelectedRoutingArchive(context, source, temporaryArchive)
        staging.mkdirs()

        ZipFile(temporaryArchive).use { archive ->
            val manifestEntry = archive.getEntry(ROUTING_MANIFEST_PATH)
                ?: error("Routing package is missing $ROUTING_MANIFEST_PATH")
            check(!manifestEntry.isDirectory) { "$ROUTING_MANIFEST_PATH is not a file" }
            check(manifestEntry.size in 1..MAX_ROUTING_MANIFEST_BYTES) {
                "Routing package manifest is too large"
            }

            val manifestText = archive.getInputStream(manifestEntry).bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
            check(manifestText.toByteArray(Charsets.UTF_8).size <= MAX_ROUTING_MANIFEST_BYTES) {
                "Routing manifest exceeds the safety limit"
            }
            val manifest = JSONObject(manifestText)
            val payloads = validateRoutingManifest(manifest)

            val allowedFiles = payloads.mapTo(mutableSetOf(ROUTING_MANIFEST_PATH)) { it.expected.path }
            val entries = archive.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                validateRoutingArchivePath(entry.name)
                if (!entry.isDirectory) {
                    check(entry.name in allowedFiles) { "Unexpected file in routing package: ${entry.name}" }
                }
            }

            payloads.forEach { payload ->
                val entry = archive.getEntry(payload.expected.path)
                    ?: error("Routing package is missing ${payload.expected.path}")
                check(!entry.isDirectory) { "Routing payload is not a file: ${payload.expected.path}" }
                check(entry.size == payload.bytes) {
                    "ZIP size does not match routing manifest for ${payload.expected.path}"
                }

                val destination = staging.resolve(payload.expected.path)
                destination.parentFile?.mkdirs()
                val digest = MessageDigest.getInstance("SHA-256")
                var copied = 0L
                archive.getInputStream(entry).use { input ->
                    destination.outputStream().buffered().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            copied += count
                            check(copied <= payload.bytes && copied <= payload.expected.maxBytes) {
                                "Routing payload exceeds declared/safe size: ${payload.expected.path}"
                            }
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                        }
                    }
                }
                check(copied == payload.bytes) { "Incomplete routing payload: ${payload.expected.path}" }
                val actualSha = digest.digest().joinToString("") { byte ->
                    "%02x".format(byte.toInt() and 0xff)
                }
                check(actualSha == payload.sha256) {
                    "SHA-256 mismatch: ${payload.expected.path}"
                }
            }

            staging.resolve(ROUTING_MANIFEST_PATH).writeText(
                manifest.toString(2) + "\n",
                Charsets.UTF_8,
            )
        }

        check(loadRoutingPackageFromDirectory(staging) != null) {
            "Staged routing package failed final validation"
        }

        var previousMoved = false
        if (installed.exists()) {
            check(installed.renameTo(backup)) {
                "Could not stage the previous routing package for replacement"
            }
            previousMoved = true
        }

        if (!staging.renameTo(installed)) {
            if (previousMoved) backup.renameTo(installed)
            error("Could not atomically install the validated routing package")
        }

        if (previousMoved) backup.deleteRecursively()
        return requireNotNull(loadRoutingPackageFromDirectory(installed)) {
            "Installed routing package could not be reopened"
        }
    } finally {
        temporaryArchive.delete()
        staging.deleteRecursively()
        if (backup.exists() && installed.exists()) backup.deleteRecursively()
    }
}

private fun copySelectedRoutingArchive(context: Context, source: Uri, destination: File) {
    val input = context.contentResolver.openInputStream(source)
        ?: error("Android could not open the selected routing package")
    input.use {
        destination.outputStream().buffered().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var copied = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                copied += count
                check(copied <= MAX_ROUTING_ARCHIVE_BYTES) {
                    "Selected routing package exceeds the spike safety limit"
                }
                output.write(buffer, 0, count)
            }
        }
    }
    check(destination.length() > 0L) { "Selected routing package is empty" }
}

private fun validateRoutingManifest(manifest: JSONObject): List<ValidatedRoutingPayload> {
    check(manifest.getInt("schemaVersion") == ROUTING_PACKAGE_SCHEMA_VERSION) {
        "Unsupported routing package schema"
    }
    check(manifest.getString("packageId") == ROUTING_PACKAGE_ID) {
        "Unexpected routing package ID"
    }
    check(manifest.getString("travelMode") == TravelMode.WALK.name) {
        "Routing package is not WALK data"
    }
    check(!manifest.optBoolean("runtimeNetworkRequired", true)) {
        "Routing package declares a runtime network requirement"
    }

    val engine = manifest.getJSONObject("routingEngine")
    check(engine.getString("name") == ROUTING_ENGINE_NAME) { "Unexpected routing engine" }
    check(engine.getString("version") == ROUTING_ENGINE_VERSION) {
        "Routing package was built for a different BRouter version"
    }

    val payloads = manifest.getJSONObject("payloads")
    return expectedRoutingPayloads.map { expected ->
        val payload = payloads.getJSONObject(expected.key)
        val path = payload.getString("path")
        check(path == expected.path) { "Unexpected ${expected.key} routing payload path" }
        validateRoutingArchivePath(path)

        val bytes = payload.getLong("bytes")
        check(bytes in 1..expected.maxBytes) { "Unsafe ${expected.key} routing payload size" }
        val hash = payload.getString("sha256").lowercase()
        check(hash.matches(Regex("[0-9a-f]{64}"))) {
            "Invalid SHA-256 for ${expected.key} routing payload"
        }
        ValidatedRoutingPayload(expected, bytes, hash)
    }
}

private fun validateRoutingArchivePath(path: String) {
    check(path.isNotBlank()) { "Blank routing package path" }
    check(!path.startsWith('/') && !path.startsWith('\\')) {
        "Absolute routing package path is forbidden"
    }
    check('\\' !in path) { "Backslash routing package path is forbidden" }
    check(path.split('/').none { it == ".." || it.isBlank() }) {
        "Unsafe routing package path: $path"
    }
}

private fun loadRoutingPackageFromDirectory(directory: File): InstalledOfflineRoutingPackage? {
    val manifestFile = directory.resolve(ROUTING_MANIFEST_PATH)
    if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_ROUTING_MANIFEST_BYTES) return null

    return runCatching {
        val payloads = validateRoutingManifest(JSONObject(manifestFile.readText(Charsets.UTF_8)))
        payloads.forEach { payload ->
            val file = directory.resolve(payload.expected.path)
            check(file.isFile && file.length() == payload.bytes) {
                "Routing package payload is incomplete: ${payload.expected.path}"
            }
        }
        InstalledOfflineRoutingPackage(
            segmentDirectory = directory.resolve("segments4"),
            profile = directory.resolve(ROUTING_PROFILE_PATH),
            lookups = directory.resolve(ROUTING_LOOKUPS_PATH),
        )
    }.getOrNull()
}

private fun routingPackageRoot(context: Context): File = context.filesDir.resolve("offline_routing")

private fun installedRoutingPackageDirectory(context: Context): File =
    routingPackageRoot(context).resolve(ROUTING_PACKAGE_ID)
