package com.deliveryapp.customer.data.dto

data class OsrmRouteResponse(
    val code: String = "",
    val routes: List<OsrmRoute> = emptyList()
)

data class OsrmRoute(
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val geometry: OsrmGeometry = OsrmGeometry()
)

data class OsrmGeometry(
    val coordinates: List<List<Double>> = emptyList()
)

data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)

data class RoutePath(
    val points: List<RoutePoint>,
    val distanceKm: Double,
    val durationMinutes: Double,
    val isFallback: Boolean = false
)
