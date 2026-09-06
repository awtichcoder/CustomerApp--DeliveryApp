package com.deliveryapp.customer.feature.tracking

import com.deliveryapp.customer.MainDispatcherRule
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.DeliveryResponse
import com.deliveryapp.customer.data.dto.DriverLocationResponse
import com.deliveryapp.customer.data.dto.MatchingStatusResponse
import com.deliveryapp.customer.data.dto.RoutePath
import com.deliveryapp.customer.data.dto.RoutePoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun polling_updatesLocationAndStops() = runTest(mainDispatcherRule.dispatcher) {
        val repository = AssignedDeliveryRepository()
        val viewModel = TrackingViewModel("order-1", repository, pollIntervalMs = 10)

        viewModel.startPolling()
        runCurrent()

        assertNotNull(viewModel.state.value.location)
        assertFalse(viewModel.state.value.isConnectionLost)

        advanceTimeBy(25)
        runCurrent()
        viewModel.stopPolling()
        val callsAfterStop = repository.locationCalls

        advanceTimeBy(100)
        runCurrent()

        assertTrue(callsAfterStop >= 2)
        assertEquals(callsAfterStop, repository.locationCalls)
    }

    @Test
    fun assignedDriver_loadsRouteFromDriverToPickup() = runTest(mainDispatcherRule.dispatcher) {
        val deliveryRepository = AssignedDeliveryRepository()
        val routingRepository = RecordingRoutingRepository()
        val viewModel = TrackingViewModel(
            orderId = "order-1",
            repository = deliveryRepository,
            pollIntervalMs = 1_000,
            routingRepository = routingRepository
        )

        viewModel.startPolling()
        runCurrent()

        assertEquals(10.780001, routingRepository.endLatitude, 0.000001)
        assertEquals(106.690001, routingRepository.endLongitude, 0.000001)
        assertNotNull(viewModel.state.value.route)
        assertEquals("Đường đến điểm lấy hàng", viewModel.state.value.routeDestinationLabel)
        viewModel.stopPolling()
    }
}

private class AssignedDeliveryRepository : DeliveryRepository {
    var locationCalls = 0

    override suspend fun getByOrder(orderId: String) = ApiResult.Success(
        DeliveryResponse(
            deliveryId = "delivery-1",
            orderId = orderId,
            customerId = "customer-1",
            driverId = "driver-1",
            status = "ASSIGNED",
            totalFee = 59_520,
            version = 2,
            pickupLatitude = 10.780001,
            pickupLongitude = 106.690001,
            dropoffLatitude = 10.790001,
            dropoffLongitude = 106.700001
        )
    )

    override suspend fun getMatchingStatus(orderId: String) = ApiResult.Success(
        MatchingStatusResponse(orderId, "ASSIGNED", "MATCHED", 3.0, 3, 1, 3, 2, "now", "now")
    )

    override suspend fun getDriverLocation(deliveryId: String): ApiResult<DriverLocationResponse> {
        locationCalls++
        return ApiResult.Success(
            DriverLocationResponse(deliveryId, "driver-1", 10.776889, 106.700806, "now", 5.0)
        )
    }
}

private class RecordingRoutingRepository : RoutingRepository {
    var endLatitude = 0.0
    var endLongitude = 0.0

    override suspend fun getRoute(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): ApiResult<RoutePath> {
        this.endLatitude = endLatitude
        this.endLongitude = endLongitude
        return ApiResult.Success(
            RoutePath(
                points = listOf(
                    RoutePoint(startLatitude, startLongitude),
                    RoutePoint(endLatitude, endLongitude)
                ),
                distanceKm = 2.4,
                durationMinutes = 8.0
            )
        )
    }
}
