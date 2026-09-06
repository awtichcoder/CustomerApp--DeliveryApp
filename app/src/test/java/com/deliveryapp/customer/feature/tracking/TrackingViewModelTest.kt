package com.deliveryapp.customer.feature.tracking

import com.deliveryapp.customer.MainDispatcherRule
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.DeliveryResponse
import com.deliveryapp.customer.data.dto.DriverLocationResponse
import com.deliveryapp.customer.data.dto.MatchingStatusResponse
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
}

private class AssignedDeliveryRepository : DeliveryRepository {
    var locationCalls = 0

    override suspend fun getByOrder(orderId: String) = ApiResult.Success(
        DeliveryResponse("delivery-1", orderId, "customer-1", "driver-1", "ASSIGNED", 59_520, 2)
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
