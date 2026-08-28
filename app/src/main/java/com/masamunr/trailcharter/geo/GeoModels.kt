package com.masamunr.trailcharter.geo

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees" }
    }
}

data class GeoBounds(
    val southWest: GeoPoint,
    val northEast: GeoPoint,
) {
    init {
        require(southWest.latitude <= northEast.latitude) { "South must not be north of north" }
    }
}

data class RouteGeometry(
    val points: List<GeoPoint>,
) {
    init {
        require(points.size >= 2) { "A route geometry requires at least two points" }
    }
}
