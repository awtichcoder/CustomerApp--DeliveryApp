package com.deliveryapp.customer.feature.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.AddressSuggestion
import com.deliveryapp.customer.data.dto.CreateOrderRequest
import com.deliveryapp.customer.data.dto.GeoPointRequest
import com.deliveryapp.customer.data.dto.OrderResponse
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val pickupSuggestions: List<AddressSuggestion> = emptyList(),
    val dropoffSuggestions: List<AddressSuggestion> = emptyList(),
    val isSearchingPickup: Boolean = false,
    val isSearchingDropoff: Boolean = false,
    val isLocatingPickup: Boolean = false,
    val locationMessage: String? = null,
    val receiverPhone: String = "",
    val promotionCode: String = "",
    val pointsToUse: String = "0",
    val clientRequestId: String = UUID.randomUUID().toString(),
    val fieldErrors: Map<CreateOrderField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val createdOrder: OrderResponse? = null
)

class CreateOrderViewModel(
    private val repository: OrderRepository,
    private val addressRepository: AddressRepository = EmptyAddressRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CreateOrderUiState())
    val state: StateFlow<CreateOrderUiState> = _state.asStateFlow()
    private var pickupSearchJob: Job? = null
    private var dropoffSearchJob: Job? = null

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

    fun onAddressQueryChanged(field: CreateOrderField, value: String) {
        require(field == CreateOrderField.PickupAddress || field == CreateOrderField.DropoffAddress)
        if (field == CreateOrderField.PickupAddress) {
            pickupSearchJob?.cancel()
            _state.update {
                it.copy(
                    pickupAddress = value,
                    pickupLatitude = "",
                    pickupLongitude = "",
                    pickupSuggestions = emptyList(),
                    isSearchingPickup = false,
                    locationMessage = null,
                    fieldErrors = it.fieldErrors - CreateOrderField.PickupAddress,
                    errorMessage = null
                )
            }
            pickupSearchJob = startAddressSearch(field, value)
        } else {
            dropoffSearchJob?.cancel()
            _state.update {
                it.copy(
                    dropoffAddress = value,
                    dropoffLatitude = "",
                    dropoffLongitude = "",
                    dropoffSuggestions = emptyList(),
                    isSearchingDropoff = false,
                    fieldErrors = it.fieldErrors - CreateOrderField.DropoffAddress,
                    errorMessage = null
                )
            }
            dropoffSearchJob = startAddressSearch(field, value)
        }
    }

    private fun startAddressSearch(field: CreateOrderField, query: String): Job? {
        if (query.trim().length < MIN_ADDRESS_QUERY_LENGTH) return null
        return viewModelScope.launch {
            delay(ADDRESS_SEARCH_DEBOUNCE_MS)
            val snapshot = _state.value
            val currentQuery = if (field == CreateOrderField.PickupAddress) {
                snapshot.pickupAddress
            } else {
                snapshot.dropoffAddress
            }
            if (currentQuery != query) return@launch

            _state.update {
                if (field == CreateOrderField.PickupAddress) it.copy(isSearchingPickup = true)
                else it.copy(isSearchingDropoff = true)
            }
            when (
                val result = addressRepository.search(
                    query = query.trim(),
                    nearLatitude = snapshot.pickupLatitude.toDoubleOrNull(),
                    nearLongitude = snapshot.pickupLongitude.toDoubleOrNull()
                )
            ) {
                is ApiResult.Success -> _state.update {
                    if (field == CreateOrderField.PickupAddress && it.pickupAddress == query) {
                        it.copy(pickupSuggestions = result.data, isSearchingPickup = false)
                    } else if (field == CreateOrderField.DropoffAddress && it.dropoffAddress == query) {
                        it.copy(dropoffSuggestions = result.data, isSearchingDropoff = false)
                    } else {
                        it
                    }
                }
                is ApiResult.Error -> _state.update {
                    if (field == CreateOrderField.PickupAddress) {
                        it.copy(isSearchingPickup = false, errorMessage = result.message)
                    } else {
                        it.copy(isSearchingDropoff = false, errorMessage = result.message)
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun selectAddress(field: CreateOrderField, suggestion: AddressSuggestion) {
        require(field == CreateOrderField.PickupAddress || field == CreateOrderField.DropoffAddress)
        if (field == CreateOrderField.PickupAddress) {
            pickupSearchJob?.cancel()
            _state.update {
                it.copy(
                    pickupAddress = suggestion.label,
                    pickupLatitude = suggestion.latitude.toString(),
                    pickupLongitude = suggestion.longitude.toString(),
                    pickupSuggestions = emptyList(),
                    isSearchingPickup = false,
                    locationMessage = null,
                    fieldErrors = it.fieldErrors - CreateOrderField.PickupAddress
                )
            }
        } else {
            dropoffSearchJob?.cancel()
            _state.update {
                it.copy(
                    dropoffAddress = suggestion.label,
                    dropoffLatitude = suggestion.latitude.toString(),
                    dropoffLongitude = suggestion.longitude.toString(),
                    dropoffSuggestions = emptyList(),
                    isSearchingDropoff = false,
                    fieldErrors = it.fieldErrors - CreateOrderField.DropoffAddress
                )
            }
        }
    }

    fun startLocatingPickup() {
        pickupSearchJob?.cancel()
        _state.update {
            it.copy(
                isLocatingPickup = true,
                pickupSuggestions = emptyList(),
                locationMessage = "Đang lấy vị trí hiện tại…",
                errorMessage = null
            )
        }
    }

    fun usePickupLocation(latitude: Double, longitude: Double) {
        _state.update {
            it.copy(
                pickupAddress = "Vị trí hiện tại",
                pickupLatitude = latitude.toString(),
                pickupLongitude = longitude.toString(),
                isLocatingPickup = true,
                locationMessage = "Đã lấy GPS, đang tìm tên địa chỉ…",
                fieldErrors = it.fieldErrors - CreateOrderField.PickupAddress
            )
        }
        viewModelScope.launch {
            when (val result = addressRepository.reverse(latitude, longitude)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        pickupAddress = result.data?.label ?: "Vị trí hiện tại",
                        isLocatingPickup = false,
                        locationMessage = "Đã chọn vị trí hiện tại."
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isLocatingPickup = false,
                        locationMessage = "Đã lấy GPS nhưng chưa đọc được tên đường."
                    )
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun setPickupLocationError(message: String) {
        _state.update { it.copy(isLocatingPickup = false, locationMessage = message) }
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
        if (state.pickupAddress.isBlank()) {
            put(CreateOrderField.PickupAddress, "Nhập địa chỉ lấy hàng.")
        } else if (!hasValidCoordinates(state.pickupLatitude, state.pickupLongitude)) {
            put(CreateOrderField.PickupAddress, "Chọn một địa chỉ trong danh sách gợi ý hoặc dùng GPS.")
        }
        if (state.dropoffAddress.isBlank()) {
            put(CreateOrderField.DropoffAddress, "Nhập địa chỉ giao hàng.")
        } else if (!hasValidCoordinates(state.dropoffLatitude, state.dropoffLongitude)) {
            put(CreateOrderField.DropoffAddress, "Chọn một địa chỉ trong danh sách gợi ý.")
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

    private fun hasValidCoordinates(latitude: String, longitude: String): Boolean {
        val lat = latitude.toDoubleOrNull() ?: return false
        val lon = longitude.toDoubleOrNull() ?: return false
        return lat in -90.0..90.0 && lon in -180.0..180.0
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

    override fun onCleared() {
        pickupSearchJob?.cancel()
        dropoffSearchJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val repository: OrderRepository,
        private val addressRepository: AddressRepository = EmptyAddressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CreateOrderViewModel::class.java))
            return CreateOrderViewModel(repository, addressRepository) as T
        }
    }
}

private const val MIN_ADDRESS_QUERY_LENGTH = 3
private const val ADDRESS_SEARCH_DEBOUNCE_MS = 550L
