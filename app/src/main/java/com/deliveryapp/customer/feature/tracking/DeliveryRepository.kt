package com.deliveryapp.customer.feature.tracking

import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.core.network.safeApiCall
import com.deliveryapp.customer.data.api.DeliveryApi
import com.deliveryapp.customer.data.dto.DeliveryResponse
import com.deliveryapp.customer.data.dto.DriverLocationResponse
import com.deliveryapp.customer.data.dto.MatchingStatusResponse

interface DeliveryRepository {
    suspend fun getByOrder(orderId: String): ApiResult<DeliveryResponse>
    suspend fun getMatchingStatus(orderId: String): ApiResult<MatchingStatusResponse>
    suspend fun getDriverLocation(deliveryId: String): ApiResult<DriverLocationResponse>
}

class NetworkDeliveryRepository(private val api: DeliveryApi) : DeliveryRepository {
    override suspend fun getByOrder(orderId: String) = safeApiCall { api.getByOrder(orderId) }

    override suspend fun getMatchingStatus(orderId: String) = safeApiCall {
        api.getMatchingStatus(orderId)
    }

    override suspend fun getDriverLocation(deliveryId: String) = safeApiCall {
        api.getDriverLocation(deliveryId)
    }
}
