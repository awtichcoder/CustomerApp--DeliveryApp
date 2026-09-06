package com.deliveryapp.customer.feature.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.CreateOrderRequest
import com.deliveryapp.customer.data.dto.GeoPointRequest
import com.deliveryapp.customer.data.dto.OrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class CreateOrderField {
    PickupAddress,
    DropoffAddress,
    PickupLatitude,
    PickupLongitude,
    DropoffLatitude,
    DropoffLongitude,
    ReceiverPhone,
    PromotionCode,
    PointsToUse
}

data class CreateOrderUiState(
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val pickupLatitude: String = "",
    val pickupLongitude: String = "",
    val dropoffLatitude: String = "",
    val dropoffLongitude: String = "",
    val receiverPhone: String = "",
    val promotionCode: String = "",
    val pointsToUse: String = "0",
    val clientRequestId: String = UUID.randomUUID().toString(),
    val fieldErrors: Map<CreateOrderField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val createdOrder: OrderResponse? = null
)

class CreateOrderViewModel(private val repository: OrderRepository) : ViewModel() {
    private val _state = MutableStateFlow(CreateOrderUiState())
    val state: StateFlow<CreateOrderUiState> = _state.asStateFlow()

    fun updateField(field: CreateOrderField, value: String) {
        _state.update { current ->
            val updated = when (field) {
                CreateOrderField.PickupAddress -> current.copy(pickupAddress = value)
                CreateOrderField.DropoffAddress -> current.copy(dropoffAddress = value)
                CreateOrderField.PickupLatitude -> current.copy(pickupLatitude = value)
                CreateOrderField.PickupLongitude -> current.copy(pickupLongitude = value)
                CreateOrderField.DropoffLatitude -> current.copy(dropoffLatitude = value)
                CreateOrderField.DropoffLongitude -> current.copy(dropoffLongitude = value)
                CreateOrderField.ReceiverPhone -> current.copy(receiverPhone = value)
                CreateOrderField.PromotionCode -> current.copy(promotionCode = value)
                CreateOrderField.PointsToUse -> current.copy(pointsToUse = value)
            }
            updated.copy(fieldErrors = updated.fieldErrors - field, errorMessage = null)
        }
    }

    fun submit() {
        val snapshot = _state.value
        if (snapshot.isSubmitting) return

        val errors = validate(snapshot)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors, errorMessage = "Vui lòng kiểm tra lại thông tin.") }
            return
        }

        val request = snapshot.toRequest()
        _state.update { it.copy(isSubmitting = true, errorMessage = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            when (val result = repository.create(request)) {
                is ApiResult.Success -> _state.update {
                    it.copy(isSubmitting = false, createdOrder = result.data)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun consumeCreatedOrder() {
        _state.update {
            it.copy(createdOrder = null, clientRequestId = UUID.randomUUID().toString())
        }
    }

    private fun validate(state: CreateOrderUiState): Map<CreateOrderField, String> = buildMap {
        if (state.pickupAddress.isBlank()) put(CreateOrderField.PickupAddress, "Nhập địa chỉ lấy hàng.")
        if (state.dropoffAddress.isBlank()) put(CreateOrderField.DropoffAddress, "Nhập địa chỉ giao hàng.")
        validateCoordinate(state.pickupLatitude, -90.0, 90.0)?.let {
            put(CreateOrderField.PickupLatitude, it)
        }
        validateCoordinate(state.pickupLongitude, -180.0, 180.0)?.let {
            put(CreateOrderField.PickupLongitude, it)
        }
        validateCoordinate(state.dropoffLatitude, -90.0, 90.0)?.let {
            put(CreateOrderField.DropoffLatitude, it)
        }
        validateCoordinate(state.dropoffLongitude, -180.0, 180.0)?.let {
            put(CreateOrderField.DropoffLongitude, it)
        }
        if (!state.receiverPhone.matches(Regex("^[0-9+][0-9]{8,14}$"))) {
            put(CreateOrderField.ReceiverPhone, "Số điện thoại phải có 9–15 chữ số.")
        }
        if (state.promotionCode.length > 50) {
            put(CreateOrderField.PromotionCode, "Mã ưu đãi tối đa 50 ký tự.")
        }
        val points = state.pointsToUse.toIntOrNull()
        if (points == null || points < 0) {
            put(CreateOrderField.PointsToUse, "Điểm sử dụng phải là số không âm.")
        }
    }

    private fun validateCoordinate(value: String, min: Double, max: Double): String? {
        val number = value.toDoubleOrNull() ?: return "Tọa độ không hợp lệ."
        return if (number in min..max) null else "Tọa độ phải trong khoảng $min đến $max."
    }

    private fun CreateOrderUiState.toRequest() = CreateOrderRequest(
        pickupAddress = pickupAddress.trim(),
        dropoffAddress = dropoffAddress.trim(),
        pickupLocation = GeoPointRequest(pickupLatitude.toDouble(), pickupLongitude.toDouble()),
        dropoffLocation = GeoPointRequest(dropoffLatitude.toDouble(), dropoffLongitude.toDouble()),
        receiverPhone = receiverPhone.trim(),
        promotionCode = promotionCode.trim().ifBlank { null },
        pointsToUse = pointsToUse.toInt(),
        clientRequestId = clientRequestId
    )

    class Factory(private val repository: OrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CreateOrderViewModel::class.java))
            return CreateOrderViewModel(repository) as T
        }
    }
}
