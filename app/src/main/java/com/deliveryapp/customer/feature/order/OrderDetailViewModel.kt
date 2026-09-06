package com.deliveryapp.customer.feature.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.MatchingStatusResponse
import com.deliveryapp.customer.data.dto.OrderResponse
import com.deliveryapp.customer.feature.tracking.DeliveryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val order: OrderResponse? = null,
    val matching: MatchingStatusResponse? = null,
    val isLoading: Boolean = false,
    val isCancelling: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class OrderDetailViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository,
    private val deliveryRepository: DeliveryRepository,
    private val pollIntervalMs: Long = 3_000L
) : ViewModel() {
    private val _state = MutableStateFlow(OrderDetailUiState())
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()
    private var matchingJob: Job? = null

    init {
        loadOrder()
    }

    fun loadOrder() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = orderRepository.getById(orderId)) {
                is ApiResult.Success -> _state.update {
                    it.copy(order = result.data, isLoading = false)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun startMatchingPolling() {
        if (matchingJob?.isActive == true) return
        matchingJob = viewModelScope.launch {
            while (isActive) {
                when (val result = deliveryRepository.getMatchingStatus(orderId)) {
                    is ApiResult.Success -> {
                        _state.update { it.copy(matching = result.data, errorMessage = null) }
                        if (result.data.status.isTerminalDeliveryStatus() || result.data.searchStatus.isTerminalSearchStatus()) {
                            loadOrder()
                            break
                        }
                        if (result.data.status == "ASSIGNED" || result.data.searchStatus == "MATCHED") {
                            loadOrder()
                            break
                        }
                    }
                    is ApiResult.Error -> {
                        if (result.httpStatus != 404) {
                            _state.update { it.copy(errorMessage = result.message) }
                        }
                    }
                    ApiResult.Loading -> Unit
                }
                delay(pollIntervalMs)
            }
        }
    }

    fun stopMatchingPolling() {
        matchingJob?.cancel()
        matchingJob = null
    }

    fun cancelOrder(reasonCode: String) {
        val order = _state.value.order ?: return
        if (_state.value.isCancelling) return
        if (reasonCode.isBlank()) {
            _state.update { it.copy(errorMessage = "Vui lòng nhập lý do hủy đơn.") }
            return
        }
        _state.update { it.copy(isCancelling = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            when (val result = orderRepository.cancel(order.orderId, reasonCode.trim(), order.version)) {
                is ApiResult.Success -> {
                    stopMatchingPolling()
                    _state.update {
                        it.copy(
                            order = result.data,
                            isCancelling = false,
                            infoMessage = "Đơn hàng đã được hủy."
                        )
                    }
                }
                is ApiResult.Error -> {
                    if (result.code == "ORDER_VERSION_CONFLICT") {
                        val refreshed = orderRepository.getById(orderId)
                        _state.update {
                            it.copy(
                                order = (refreshed as? ApiResult.Success)?.data ?: it.order,
                                isCancelling = false,
                                errorMessage = "Đơn hàng vừa thay đổi. Dữ liệu mới đã được tải lại, hãy kiểm tra trước khi hủy."
                            )
                        }
                    } else {
                        _state.update { it.copy(isCancelling = false, errorMessage = result.message) }
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        stopMatchingPolling()
        super.onCleared()
    }

    class Factory(
        private val orderId: String,
        private val orderRepository: OrderRepository,
        private val deliveryRepository: DeliveryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(OrderDetailViewModel::class.java))
            return OrderDetailViewModel(orderId, orderRepository, deliveryRepository) as T
        }
    }
}

fun String.isTerminalDeliveryStatus(): Boolean = this in setOf("COMPLETED", "CANCELLED", "NO_DRIVER_FOUND")

private fun String.isTerminalSearchStatus(): Boolean = this in setOf("MATCHED", "CANCELLED", "NO_DRIVER_FOUND")
