package com.deliveryapp.customer.feature.order

import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.core.network.safeApiCall
import com.deliveryapp.customer.data.api.LoyaltyApi
import com.deliveryapp.customer.data.api.OrderApi
import com.deliveryapp.customer.data.api.PromotionApi
import com.deliveryapp.customer.data.dto.*

interface CheckoutRepository {
    suspend fun promotions(): ApiResult<List<PromotionResponse>>
    suspend fun preview(request: CreateOrderRequest): ApiResult<CheckoutPreview>
}

class NetworkCheckoutRepository(
    private val orders: OrderApi,
    private val loyalty: LoyaltyApi,
    private val promotionApi: PromotionApi
) : CheckoutRepository {
    override suspend fun promotions() = safeApiCall { promotionApi.getActive() }

    override suspend fun preview(request: CreateOrderRequest) = safeApiCall {
        val price = orders.preview(request)
        val points = loyalty.preview(PreviewPointsRequest(request.pointsToUse, price.amountAfterPromotion))
        CheckoutPreview(price, points)
    }
}
