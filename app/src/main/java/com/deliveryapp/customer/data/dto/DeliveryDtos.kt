package com.deliveryapp.customer.data.dto

data class MatchingStatusResponse(
    val orderId: String,
    val status: String,
    val searchStatus: String,
    val currentRadiusKm: Double,
    val invitedDrivers: Int,
    val attemptCount: Int,
    val candidateCount: Int,
    val remainingCandidateCount: Int,
    val searchStartedAt: String,
    val searchEndedAt: String?
)
data class DeliveryResponse(
    val deliveryId: String,
    val orderId: String,
    val customerId: String,
    val driverId: String?,
    val status: String,
    val totalFee: Long,
    val version: Int
)
data class DriverLocationResponse(
    val deliveryId: String,
    val driverId: String,
    val latitude: Double,
    val longitude: Double,
    val recordedAt: String,
    val accuracyM: Double?
)
