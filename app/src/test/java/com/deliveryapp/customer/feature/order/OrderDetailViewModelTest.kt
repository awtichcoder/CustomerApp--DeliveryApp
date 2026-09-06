package com.deliveryapp.customer.feature.order

import com.deliveryapp.customer.MainDispatcherRule
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.CreateOrderRequest
import com.deliveryapp.customer.data.dto.DeliveryResponse
import com.deliveryapp.customer.data.dto.DriverLocationResponse
import com.deliveryapp.customer.data.dto.MatchingStatusResponse
import com.deliveryapp.customer.data.dto.OrderResponse
import com.deliveryapp.customer.data.dto.PagedResponse
import com.deliveryapp.customer.feature.tracking.DeliveryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun matchingPolling_stopsWhenScreenStops() = runTest(mainDispatcherRule.dispatcher) {
        val orders = ConflictOrderRepository()
        val deliveries = SearchingDeliveryRepository()
        val viewModel = OrderDetailViewModel("order-1", orders, deliveries, pollIntervalMs = 10)
        runCurrent()

        viewModel.startMatchingPolling()
        runCurrent()
        advanceTimeBy(25)
        runCurrent()
        viewModel.stopMatchingPolling()
        val callsAfterStop = deliveries.matchingCalls

        advanceTimeBy(100)
        runCurrent()

        assertTrue(callsAfterStop >= 2)
        assertEquals(callsAfterStop, deliveries.matchingCalls)
    }

    @Test
    fun versionConflict_refreshesLatestOrder() = runTest(mainDispatcherRule.dispatcher) {
        val orders = ConflictOrderRepository()
        val viewModel = OrderDetailViewModel(
            "order-1",
            orders,
            SearchingDeliveryRepository(),
            pollIntervalMs = 10
        )
        runCurrent()

        viewModel.cancelOrder("CUSTOMER_CHANGED_MIND")
        runCurrent()

        assertEquals(2, viewModel.state.value.order?.version)
        assertTrue(viewModel.state.value.errorMessage?.contains("Dữ liệu mới") == true)
    }
}

private class ConflictOrderRepository : OrderRepository {
    private var reads = 0

    override suspend fun create(request: CreateOrderRequest) = ApiResult.Success(sampleOrder())

    override suspend fun getOrders(page: Int, pageSize: Int, status: String?) =
        ApiResult.Success(PagedResponse<OrderResponse>(page, pageSize, 1, listOf(sampleOrder())))

    override suspend fun getById(orderId: String): ApiResult<OrderResponse> {
        reads++
        return ApiResult.Success(sampleOrder(version = if (reads == 1) 1 else 2))
    }

    override suspend fun cancel(orderId: String, reasonCode: String, version: Int) =
        ApiResult.Error("ORDER_VERSION_CONFLICT", "Version hiện tại là 2.", 409)
}

private class SearchingDeliveryRepository : DeliveryRepository {
    var matchingCalls = 0

    override suspend fun getByOrder(orderId: String) = ApiResult.Success(
        DeliveryResponse("delivery-1", orderId, "customer-1", null, "SEARCHING", 59_520, 1)
    )

    override suspend fun getMatchingStatus(orderId: String): ApiResult<MatchingStatusResponse> {
        matchingCalls++
        return ApiResult.Success(
            MatchingStatusResponse(
                orderId = orderId,
                status = "SEARCHING",
                searchStatus = "SEARCHING",
                currentRadiusKm = 3.0,
                invitedDrivers = 3,
                attemptCount = 1,
                candidateCount = 3,
                remainingCandidateCount = 3,
                searchStartedAt = "2026-09-04T10:24:06Z",
                searchEndedAt = null
            )
        )
    }

    override suspend fun getDriverLocation(deliveryId: String) = ApiResult.Error(
        "DRIVER_LOCATION_NOT_FOUND",
        "Chưa có vị trí.",
        404
    )
}
