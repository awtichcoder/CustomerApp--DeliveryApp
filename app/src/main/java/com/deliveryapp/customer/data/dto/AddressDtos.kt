package com.deliveryapp.customer.data.dto

data class PhotonResponse(val features: List<PhotonFeature> = emptyList())

data class PhotonFeature(
    val geometry: PhotonGeometry = PhotonGeometry(),
    val properties: PhotonProperties = PhotonProperties()
)

data class PhotonGeometry(val coordinates: List<Double> = emptyList())

data class PhotonProperties(
    val name: String? = null,
    val housenumber: String? = null,
    val street: String? = null,
    val locality: String? = null,
    val district: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null
)

data class AddressSuggestion(
    val label: String,
    val latitude: Double,
    val longitude: Double
)
