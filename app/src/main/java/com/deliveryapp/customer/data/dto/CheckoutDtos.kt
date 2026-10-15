package com.deliveryapp.customer.data.dto

data class PromotionResponse(
    val code: String,
    val description: String,
    val discountAmount: Long?,
    val discountPercent: Double?,
    val maxDiscount: Long?,
    val minOrderAmount: Long,
    val endAt: String
)

data class OrderPricePreview(
    val distanceKm: Double,
    val baseFee: Long,
    val surcharge: Long,
    val platformFee: Long,
    val shippingFee: Long,
    val discountPromotion: Long,
    val amountAfterPromotion: Long
)

data class CheckoutPreview(val price: OrderPricePreview, val points: PointsPreviewResponse) {
    val totalFee: Long get() = (price.amountAfterPromotion - points.discountAmount).coerceAtLeast(0)
}
