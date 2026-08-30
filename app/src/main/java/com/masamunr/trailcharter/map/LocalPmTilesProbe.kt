package com.masamunr.trailcharter.map

import android.content.Context
import java.io.File
import java.util.Base64

private const val probeFileName = "trailcharter-map-spike.pmtiles"
private const val probeFileSizeBytes = 348L

/**
 * Writes a tiny deterministic PMTiles v3 vector fixture into TrailCharter's app-managed files.
 *
 * The fixture contains one z0 MVT line layer named `route`. It is deliberately synthetic and
 * exists only to prove that the renderer can consume a local PMTiles file without networking.
 */
internal fun ensureLocalPmTilesProbe(context: Context): File {
    val file = context.filesDir.resolve(probeFileName)
    if (!file.exists() || file.length() != probeFileSizeBytes) {
        file.writeBytes(Base64.getDecoder().decode(localPmTilesProbeBase64))
    }
    check(file.length() == probeFileSizeBytes) { "Local PMTiles probe fixture is incomplete" }
    return file
}

internal fun localPmTilesStyle(file: File): String {
    val escapedPath = file.absolutePath
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

    return """
        {
          "version": 8,
          "name": "TrailCharter local PMTiles probe",
          "center": [0, 0],
          "zoom": 0,
          "sources": {
            "probe-route": {
              "type": "vector",
              "url": "pmtiles://file://$escapedPath"
            }
          },
          "layers": [
            {
              "id": "background",
              "type": "background",
              "paint": { "background-color": "#1F3D2E" }
            },
            {
              "id": "probe-route-line",
              "type": "line",
              "source": "probe-route",
              "source-layer": "route",
              "paint": {
                "line-color": "#F4E7C5",
                "line-width": 5
              }
            }
          ]
        }
    """.trimIndent()
}

private const val localPmTilesProbeBase64 =
    "UE1UaWxlcwN/AAAAAAAAAAUAAAAAAAAAhAAAAAAAAAC7AAAAAAAAAD8BAAAAAAAAAAAAAAAAAAA/AQAAAAAAAB0AAAAAAAAAAQAAAAAAAAABAAAAAAAAAAEAAAAAAAAAAQEBAQAAAC62lIAHVs0A0klrgPipMgAAAAAAAAAAAAEAAR0BeyJuYW1lIjoiVHJhaWxDaGFydGVyIGxvY2FsIFBNVGlsZXMgcHJvYmUiLCJkZXNjcmlwdGlvbiI6IlN5bnRoZXRpYyB2ZWN0b3IgZml4dHVyZSBmb3Igb2ZmbGluZSByZW5kZXJlciB2ZXJpZmljYXRpb24iLCJ2ZWN0b3JfbGF5ZXJzIjpbeyJpZCI6InJvdXRlIiwiZmllbGRzIjp7fSwibWluem9vbSI6MCwibWF4em9vbSI6MH1dfRobCgVyb3V0ZRINGAIiCQmACIAgCoAwACiAIHgC"
