package com.masamunr.trailcharter.map

import com.masamunr.trailcharter.geo.GeoBounds

enum class MapLayerCapability {
    BASE_VECTOR,
    TOPOGRAPHIC_CONTOURS,
    SHADED_RELIEF,
    AERIAL_IMAGERY,
    LIVE_TRAFFIC,
}

data class MapRendererCapabilities(
    val supportedLayers: Set<MapLayerCapability>,
    val supportsOfflineLocalPackages: Boolean,
)

interface MapRendererBoundary {
    val capabilities: MapRendererCapabilities
}

data class OfflineMapPackage(
    val id: String,
    val displayName: String,
    val bounds: GeoBounds,
    val localPath: String,
    val layers: Set<MapLayerCapability>,
)

interface OfflineMapPackageStore {
    suspend fun availablePackages(): List<OfflineMapPackage>
}
