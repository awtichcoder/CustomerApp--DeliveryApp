package com.deliveryapp.customer.feature.tracking

import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.api.RoutingApi
import com.deliveryapp.customer.data.dto.RoutePath
import com.deliveryapp.customer.data.dto.RoutePoint
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

interface RoutingRepository {
    suspend fun getRoute(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): ApiResult<RoutePath>
}

class NetworkRoutingRepository(private val api: RoutingApi) : RoutingRepository {
    override suspend fun getRoute(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): ApiResult<RoutePath> = try {
        val coordinates = "$startLongitude,$startLatitude;$endLongitude,$endLatitude"
        val response = api.getRoute(coordinates)
        val route = response.routes.firstOrNull()
        if (response.code != "Ok" || route == null) {
            ApiResult.Success(fallbackRoute(startLatitude, startLongitude, endLatitude, endLongitude))
        } else {
            val points = route.geometry.coordinates.mapNotNull { coordinate ->
                val longitude = coordinate.getOrNull(0) ?: return@mapNotNull null
                val latitude = coordinate.getOrNull(1) ?: return@mapNotNull null
                RoutePoint(latitude = latitude, longitude = longitude)
            }
            if (points.size < 2) {
                ApiResult.Success(fallbackRoute(startLatitude, startLongitude, endLatitude, endLongitude))
            } else {
                ApiResult.Success(
                    RoutePath(
                        points = points,
                        distanceKm = route.distance / 1_000.0,
                        durationMinutes = route.duration / 60.0
                    )
                )
            }
        }
    } catch (error: CancellationException) {
        throw error
    } catch (_: IOException) {
        ApiResult.Success(fallbackRoute(startLatitude, startLongitude, endLatitude, endLongitude))
    } catch (error: HttpException) {
        ApiResult.Success(fallbackRoute(startLatitude, startLongitude, endLatitude, endLongitude))
    } catch (_: Exception) {
        ApiResult.Success(fallbackRoute(startLatitude, startLongitude, endLatitude, endLongitude))
    }
}

private fun fallbackRoute(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double
): RoutePath {
    val lat1 = Math.toRadians(startLatitude)
    val lat2 = Math.toRadians(endLatitude)
    val deltaLat = Math.toRadians(endLatitude - startLatitude)
    val deltaLongitude = Math.toRadians(endLongitude - startLongitude)
    val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
        cos(lat1) * cos(lat2) * sin(deltaLongitude / 2) * sin(deltaLongitude / 2)
    val distanceKm = 6_371.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
    return RoutePath(
        points = listOf(
            RoutePoint(startLatitude, startLongitude),
            RoutePoint(endLatitude, endLongitude)
        ),
        distanceKm = distanceKm,
        durationMinutes = distanceKm / 30.0 * 60.0,
        isFallback = true
    )
}

object EmptyRoutingRepository : RoutingRepository {
    override suspend fun getRoute(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): ApiResult<RoutePath> = ApiResult.Error("ROUTE_DISABLED", "Chưa cấu hình dịch vụ tìm đường.")
}
