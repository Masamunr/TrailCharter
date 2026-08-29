package com.masamunr.trailcharter.map

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipFile
import org.json.JSONObject

internal data class InstalledOfflineMapPackage(
    val basemap: File,
    val terrain: File,
    val contours: File,
    val glyphDirectory: File,
)

private const val PACKAGE_SCHEMA_VERSION = 1
private const val PACKAGE_ID = "uk-wales-eryri-pass3-spike"
private const val MANIFEST_PATH = "manifest.json"
private const val BASEMAP_PATH = "eryri-basemap.pmtiles"
private const val TERRAIN_PATH = "eryri-terrain.pmtiles"
private const val CONTOURS_PATH = "eryri-contours.pmtiles"
private const val GLYPH_PATH = "glyphs/TrailCharterSans/0-255.pbf"

private const val MAX_ARCHIVE_BYTES = 320L * 1024L * 1024L
private const val MAX_MANIFEST_BYTES = 1024L * 1024L
private const val MAX_BASEMAP_BYTES = 32L * 1024L * 1024L
private const val MAX_TERRAIN_BYTES = 180L * 1024L * 1024L
private const val MAX_CONTOURS_BYTES = 120L * 1024L * 1024L
private const val MAX_GLYPH_BYTES = 2L * 1024L * 1024L

private data class ExpectedPayload(
    val layerKey: String,
    val path: String,
    val maxBytes: Long,
)

private data class ValidatedPayload(
    val expected: ExpectedPayload,
    val bytes: Long,
    val sha256: String,
)

private val expectedPayloads = listOf(
    ExpectedPayload("basemap", BASEMAP_PATH, MAX_BASEMAP_BYTES),
    ExpectedPayload("terrain", TERRAIN_PATH, MAX_TERRAIN_BYTES),
    ExpectedPayload("contours", CONTOURS_PATH, MAX_CONTOURS_BYTES),
    ExpectedPayload("glyphs", GLYPH_PATH, MAX_GLYPH_BYTES),
)

/**
 * Returns the currently installed Eryri spike package, if a complete validated install exists.
 *
 * Hashes are checked at import time. On ordinary startup we re-check the immutable manifest and
 * byte sizes only, avoiding a 100+ MiB re-hash every time the map opens.
 */
internal fun loadInstalledEryriMapPackage(context: Context): InstalledOfflineMapPackage? {
    val directory = installedPackageDirectory(context)
    val manifestFile = directory.resolve(MANIFEST_PATH)
    if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_MANIFEST_BYTES) return null

    return runCatching {
        val manifest = JSONObject(manifestFile.readText(Charsets.UTF_8))
        val payloads = validateManifest(manifest)
        payloads.forEach { payload ->
            val file = directory.resolve(payload.expected.path)
            check(file.isFile) { "Installed package payload is missing: ${payload.expected.path}" }
            check(file.length() == payload.bytes) {
                "Installed package payload size changed: ${payload.expected.path}"
            }
        }
        installedFiles(directory)
    }.getOrNull()
}

/**
 * Copies, validates and atomically installs a user-selected local package into app-private storage.
 * No broad storage permission is needed because the source URI comes from Android's document picker.
 */
internal fun importEryriMapPackage(context: Context, source: Uri): InstalledOfflineMapPackage {
    val temporaryArchive = context.cacheDir.resolve("map-import-${UUID.randomUUID()}.zip")
    val root = packageRoot(context).also { it.mkdirs() }
    val staging = root.resolve(".staging-${UUID.randomUUID()}")
    val backup = root.resolve(".backup-${UUID.randomUUID()}")
    val installed = installedPackageDirectory(context)

    try {
        copySelectedArchive(context, source, temporaryArchive)
        staging.mkdirs()

        ZipFile(temporaryArchive).use { archive ->
            val manifestEntry = archive.getEntry(MANIFEST_PATH)
                ?: error("Package is missing $MANIFEST_PATH")
            check(!manifestEntry.isDirectory) { "$MANIFEST_PATH is not a file" }
            check(manifestEntry.size in 1..MAX_MANIFEST_BYTES) { "Package manifest is too large" }

            val manifestText = archive.getInputStream(manifestEntry).bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
            check(manifestText.toByteArray(Charsets.UTF_8).size <= MAX_MANIFEST_BYTES) {
                "Package manifest exceeds the safety limit"
            }
            val manifest = JSONObject(manifestText)
            val payloads = validateManifest(manifest)

            val allowedFiles = payloads.mapTo(mutableSetOf(MANIFEST_PATH)) { it.expected.path }
            val entries = archive.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                validateArchivePath(entry.name)
                if (!entry.isDirectory) {
                    check(entry.name in allowedFiles) { "Unexpected file in package: ${entry.name}" }
                }
            }

            payloads.forEach { payload ->
                val entry = archive.getEntry(payload.expected.path)
                    ?: error("Package is missing ${payload.expected.path}")
                check(!entry.isDirectory) { "Payload is not a file: ${payload.expected.path}" }
                check(entry.size == payload.bytes) {
                    "ZIP size does not match manifest for ${payload.expected.path}"
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
                                "Payload exceeds declared/safe size: ${payload.expected.path}"
                            }
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                        }
                    }
                }
                check(copied == payload.bytes) { "Incomplete payload: ${payload.expected.path}" }
                val actualSha = digest.digest().joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
                check(actualSha == payload.sha256) { "SHA-256 mismatch: ${payload.expected.path}" }
            }

            staging.resolve(MANIFEST_PATH).writeText(
                manifest.toString(2) + "\n",
                Charsets.UTF_8,
            )
        }

        check(loadPackageFromDirectory(staging) != null) { "Staged package failed final validation" }

        var previousMoved = false
        if (installed.exists()) {
            check(installed.renameTo(backup)) { "Could not stage the previous package for replacement" }
            previousMoved = true
        }

        if (!staging.renameTo(installed)) {
            if (previousMoved) {
                backup.renameTo(installed)
            }
            error("Could not atomically install the validated package")
        }

        if (previousMoved) backup.deleteRecursively()
        return requireNotNull(loadPackageFromDirectory(installed)) {
            "Installed package could not be reopened"
        }
    } finally {
        temporaryArchive.delete()
        staging.deleteRecursively()
        if (backup.exists() && installed.exists()) backup.deleteRecursively()
    }
}

private fun copySelectedArchive(context: Context, source: Uri, destination: File) {
    val input = context.contentResolver.openInputStream(source)
        ?: error("Android could not open the selected package")
    input.use {
        destination.outputStream().buffered().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var copied = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                copied += count
                check(copied <= MAX_ARCHIVE_BYTES) { "Selected package exceeds the spike safety limit" }
                output.write(buffer, 0, count)
            }
        }
    }
    check(destination.length() > 0L) { "Selected package is empty" }
}

private fun validateManifest(manifest: JSONObject): List<ValidatedPayload> {
    check(manifest.getInt("schemaVersion") == PACKAGE_SCHEMA_VERSION) {
        "Unsupported package schema"
    }
    check(manifest.getString("packageId") == PACKAGE_ID) { "Unexpected package ID" }
    check(!manifest.optBoolean("runtimeNetworkRequired", true)) {
        "Package declares a runtime network requirement"
    }

    val layers = manifest.getJSONObject("layers")
    return expectedPayloads.map { expected ->
        val layer = layers.getJSONObject(expected.layerKey)
        val path = layer.getString("path")
        check(path == expected.path) { "Unexpected ${expected.layerKey} payload path" }
        validateArchivePath(path)

        val bytes = layer.getLong("bytes")
        check(bytes in 1..expected.maxBytes) { "Unsafe ${expected.layerKey} payload size" }
        val hash = layer.getString("sha256").lowercase()
        check(hash.matches(Regex("[0-9a-f]{64}"))) { "Invalid SHA-256 for ${expected.layerKey}" }
        ValidatedPayload(expected, bytes, hash)
    }
}

private fun validateArchivePath(path: String) {
    check(path.isNotBlank()) { "Blank package path" }
    check(!path.startsWith('/') && !path.startsWith('\\')) { "Absolute package path is forbidden" }
    check('\\' !in path) { "Backslash package path is forbidden" }
    check(path.split('/').none { it == ".." || it.isBlank() }) { "Unsafe package path: $path" }
}

private fun loadPackageFromDirectory(directory: File): InstalledOfflineMapPackage? {
    val manifestFile = directory.resolve(MANIFEST_PATH)
    if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_MANIFEST_BYTES) return null
    return runCatching {
        val payloads = validateManifest(JSONObject(manifestFile.readText(Charsets.UTF_8)))
        payloads.forEach { payload ->
            val file = directory.resolve(payload.expected.path)
            check(file.isFile && file.length() == payload.bytes) {
                "Package payload is incomplete: ${payload.expected.path}"
            }
        }
        installedFiles(directory)
    }.getOrNull()
}

private fun installedFiles(directory: File) = InstalledOfflineMapPackage(
    basemap = directory.resolve(BASEMAP_PATH),
    terrain = directory.resolve(TERRAIN_PATH),
    contours = directory.resolve(CONTOURS_PATH),
    glyphDirectory = directory.resolve("glyphs"),
)

private fun packageRoot(context: Context): File = context.filesDir.resolve("offline_maps")

private fun installedPackageDirectory(context: Context): File = packageRoot(context).resolve(PACKAGE_ID)
