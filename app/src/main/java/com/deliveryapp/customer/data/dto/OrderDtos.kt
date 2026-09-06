package com.deliveryapp.customer.data.dto

data class GeoPointRequest(val latitude: Double, val longitude: Double)
data class CreateOrderRequest(
    val pickupAddress: String,
    val dropoffAddress: String,
    val pickupLocation: GeoPointRequest,
    val dropoffLocation: GeoPointRequest,
    val receiverPhone: String,
    val promotionCode: String?,
    val pointsToUse: Int,
    val clientRequestId: String
)
data class CancelOrderRequest(val reasonCode: String, val version: Int?)
data class OrderResponse(
    val orderId: String,
    val customerId: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val receiverPhone: String,
    val distanceKm: Double,
    val baseFee: Long,
    val surcharge: Long,
    val platformFee: Long,
    val discountPromotion: Long,
    val discountPoints: Long,
    val totalFee: Long,
    val status: String,
    val matchingStatus: String,
    val version: Int,
    val createdAt: String,
    val updatedAt: String?,
    val refundStatus: String
)
data class PagedResponse<T>(val page: Int, val pageSize: Int, val totalItems: Int, val items: List<T>)
