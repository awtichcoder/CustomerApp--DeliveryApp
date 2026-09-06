package com.deliveryapp.customer.feature.order

import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.api.AddressSearchApi
import com.deliveryapp.customer.data.dto.AddressSuggestion
import com.deliveryapp.customer.data.dto.PhotonFeature
import java.io.IOException
import retrofit2.HttpException

interface AddressRepository {
    suspend fun search(
        query: String,
        nearLatitude: Double? = null,
        nearLongitude: Double? = null
    ): ApiResult<List<AddressSuggestion>>

    suspend fun reverse(latitude: Double, longitude: Double): ApiResult<AddressSuggestion?>
}

class NetworkAddressRepository(private val api: AddressSearchApi) : AddressRepository {
    override suspend fun search(
        query: String,
        nearLatitude: Double?,
        nearLongitude: Double?
    ): ApiResult<List<AddressSuggestion>> = addressApiCall {
        api.search(
            query = query,
            latitude = nearLatitude,
            longitude = nearLongitude
        ).features.mapNotNull(PhotonFeature::toSuggestion).distinctBy(AddressSuggestion::label)
    }

    override suspend fun reverse(
        latitude: Double,
        longitude: Double
    ): ApiResult<AddressSuggestion?> = addressApiCall {
        api.reverse(latitude, longitude).features.firstOrNull()?.toSuggestion()
    }
}

object EmptyAddressRepository : AddressRepository {
    override suspend fun search(
        query: String,
        nearLatitude: Double?,
        nearLongitude: Double?
    ): ApiResult<List<AddressSuggestion>> = ApiResult.Success(emptyList())

    override suspend fun reverse(
        latitude: Double,
        longitude: Double
    ): ApiResult<AddressSuggestion?> = ApiResult.Success(null)
}

private suspend fun <T> addressApiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (_: IOException) {
    ApiResult.Error("ADDRESS_NETWORK", "Không tải được gợi ý địa chỉ. Kiểm tra Internet rồi thử lại.")
} catch (error: HttpException) {
    ApiResult.Error("ADDRESS_HTTP_${error.code()}", "Dịch vụ gợi ý địa chỉ đang bận. Vui lòng thử lại.")
} catch (_: Exception) {
    ApiResult.Error("ADDRESS_UNKNOWN", "Không thể tìm địa chỉ lúc này.")
}

private fun PhotonFeature.toSuggestion(): AddressSuggestion? {
    val longitude = geometry.coordinates.getOrNull(0) ?: return null
    val latitude = geometry.coordinates.getOrNull(1) ?: return null
    val streetLine = listOfNotNull(properties.housenumber, properties.street)
        .joinToString(" ")
        .ifBlank { properties.name.orEmpty() }
    val label = listOfNotNull(
        streetLine.takeIf(String::isNotBlank),
        properties.name?.takeIf { it.isNotBlank() && !it.equals(streetLine, ignoreCase = true) },
        properties.locality,
        properties.district,
        properties.city,
        properties.county,
        properties.state,
        properties.country
    ).map(String::trim).filter(String::isNotBlank).distinct().joinToString(", ")

    return AddressSuggestion(
        label = label.ifBlank { "$latitude, $longitude" },
        latitude = latitude,
        longitude = longitude
    )
}
