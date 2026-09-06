package com.deliveryapp.customer.feature.order

import com.deliveryapp.customer.MainDispatcherRule
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.CreateOrderRequest
import com.deliveryapp.customer.data.dto.OrderResponse
import com.deliveryapp.customer.data.dto.PagedResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadMore_appendsNextPage() = runTest(mainDispatcherRule.dispatcher) {
        val repository = PagingOrderRepository()
        val viewModel = OrdersViewModel(repository)
        runCurrent()

        viewModel.loadMore()
        runCurrent()

        assertEquals(listOf(1, 2), repository.requestedPages)
        assertEquals(2, viewModel.state.value.items.size)
        assertFalse(viewModel.state.value.hasMore)
    }

    @Test
    fun changeFilter_reloadsFirstPageWithStatus() = runTest(mainDispatcherRule.dispatcher) {
        val repository = PagingOrderRepository()
        val viewModel = OrdersViewModel(repository)
        runCurrent()

        viewModel.changeFilter(OrderFilter.Pending)
        runCurrent()

        assertEquals("PENDING", repository.requestedStatuses.last())
        assertEquals(OrderFilter.Pending, viewModel.state.value.filter)
        assertEquals(1, viewModel.state.value.page)
    }
}

private class PagingOrderRepository : OrderRepository {
    val requestedPages = mutableListOf<Int>()
    val requestedStatuses = mutableListOf<String?>()

    override suspend fun create(request: CreateOrderRequest) = ApiResult.Success(sampleOrder())

    override suspend fun getOrders(
        page: Int,
        pageSize: Int,
        status: String?
    ): ApiResult<PagedResponse<OrderResponse>> {
        requestedPages += page
        requestedStatuses += status
        val item = sampleOrder(status = status ?: "PENDING").copy(orderId = "order-$page-${status ?: "all"}")
        val total = if (status == null) 2 else 1
        return ApiResult.Success(PagedResponse(page, pageSize, total, listOf(item)))
    }

    override suspend fun getById(orderId: String) = ApiResult.Success(sampleOrder())

    override suspend fun cancel(orderId: String, reasonCode: String, version: Int) =
        ApiResult.Success(sampleOrder(status = "CANCELLED"))
}
