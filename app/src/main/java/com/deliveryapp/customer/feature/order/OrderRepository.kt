package com.deliveryapp.customer.feature.order

import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.core.network.safeApiCall
import com.deliveryapp.customer.data.api.OrderApi
import com.deliveryapp.customer.data.dto.CancelOrderRequest
import com.deliveryapp.customer.data.dto.CreateOrderRequest
import com.deliveryapp.customer.data.dto.OrderResponse
import com.deliveryapp.customer.data.dto.PagedResponse

interface OrderRepository {
    suspend fun create(request: CreateOrderRequest): ApiResult<OrderResponse>
    suspend fun getOrders(page: Int, pageSize: Int, status: String?): ApiResult<PagedResponse<OrderResponse>>
    suspend fun getById(orderId: String): ApiResult<OrderResponse>
    suspend fun cancel(orderId: String, reasonCode: String, version: Int): ApiResult<OrderResponse>
}

class NetworkOrderRepository(private val api: OrderApi) : OrderRepository {
    override suspend fun create(request: CreateOrderRequest) = safeApiCall { api.create(request) }

    override suspend fun getOrders(page: Int, pageSize: Int, status: String?) = safeApiCall {
        api.getOrders(page = page, pageSize = pageSize, status = status)
    }

    override suspend fun getById(orderId: String) = safeApiCall { api.getById(orderId) }

    override suspend fun cancel(orderId: String, reasonCode: String, version: Int) = safeApiCall {
        api.cancel(orderId, CancelOrderRequest(reasonCode = reasonCode, version = version))
    }
}
