package com.deliveryapp.customer.feature.order

import com.deliveryapp.customer.MainDispatcherRule
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.CreateOrderRequest
import com.deliveryapp.customer.data.dto.OrderResponse
import com.deliveryapp.customer.data.dto.PagedResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateOrderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submitTwice_sendsOnlyOneRequest() = runTest(mainDispatcherRule.dispatcher) {
        val repository = DeferredCreateRepository()
        val viewModel = CreateOrderViewModel(repository)
        fillValidForm(viewModel)

        viewModel.submit()
        runCurrent()
        viewModel.submit()

        assertEquals(1, repository.createCalls)
        assertTrue(viewModel.state.value.isSubmitting)

        repository.result.complete(ApiResult.Success(sampleOrder()))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSubmitting)
        assertNotNull(viewModel.state.value.createdOrder)
    }

    @Test
    fun invalidForm_doesNotCallRepository() = runTest(mainDispatcherRule.dispatcher) {
        val repository = DeferredCreateRepository()
        val viewModel = CreateOrderViewModel(repository)

        viewModel.submit()
        runCurrent()

        assertEquals(0, repository.createCalls)
        assertTrue(viewModel.state.value.fieldErrors.isNotEmpty())
    }

    private fun fillValidForm(viewModel: CreateOrderViewModel) {
        viewModel.updateField(CreateOrderField.PickupAddress, "72 Lê Thánh Tôn")
        viewModel.updateField(CreateOrderField.DropoffAddress, "18 Phan Xích Long")
        viewModel.updateField(CreateOrderField.PickupLatitude, "10.776889")
        viewModel.updateField(CreateOrderField.PickupLongitude, "106.700806")
        viewModel.updateField(CreateOrderField.DropoffLatitude, "10.762622")
        viewModel.updateField(CreateOrderField.DropoffLongitude, "106.660172")
        viewModel.updateField(CreateOrderField.ReceiverPhone, "0912345678")
    }
}

private class DeferredCreateRepository : OrderRepository {
    var createCalls = 0
    val result = CompletableDeferred<ApiResult<OrderResponse>>()

    override suspend fun create(request: CreateOrderRequest): ApiResult<OrderResponse> {
        createCalls++
        return result.await()
    }

    override suspend fun getOrders(page: Int, pageSize: Int, status: String?) =
        ApiResult.Success(PagedResponse<OrderResponse>(page, pageSize, 0, emptyList()))

    override suspend fun getById(orderId: String) = ApiResult.Success(sampleOrder())

    override suspend fun cancel(orderId: String, reasonCode: String, version: Int) =
        ApiResult.Success(sampleOrder())
}

internal fun sampleOrder(version: Int = 1, status: String = "PENDING") = OrderResponse(
    orderId = "f480dc19-f0ea-492d-96f0-cde45973754f",
    customerId = "20000000-0000-0000-0000-000000000001",
    pickupAddress = "72 Lê Thánh Tôn",
    dropoffAddress = "18 Phan Xích Long",
    receiverPhone = "0912345678",
    distanceKm = 4.71,
    baseFee = 41_100,
    surcharge = 9_420,
    platformFee = 3_000,
    discountPromotion = 0,
    discountPoints = 0,
    totalFee = 59_520,
    status = status,
    matchingStatus = "SEARCHING",
    version = version,
    createdAt = "2026-09-04T10:24:06Z",
    updatedAt = null,
    refundStatus = "NOT_APPLICABLE"
)
