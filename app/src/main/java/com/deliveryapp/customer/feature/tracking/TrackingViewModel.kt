package com.deliveryapp.customer.feature.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.DeliveryResponse
import com.deliveryapp.customer.data.dto.DriverLocationResponse
import com.deliveryapp.customer.feature.order.isTerminalDeliveryStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TrackingUiState(
    val delivery: DeliveryResponse? = null,
    val location: DriverLocationResponse? = null,
    val isLoading: Boolean = true,
    val isConnectionLost: Boolean = false,
    val errorMessage: String? = null
)

class TrackingViewModel(
    private val orderId: String,
    private val repository: DeliveryRepository,
    private val pollIntervalMs: Long = 5_000L
) : ViewModel() {
    private val _state = MutableStateFlow(TrackingUiState())
    val state: StateFlow<TrackingUiState> = _state.asStateFlow()
    private var pollingJob: Job? = null

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                when (val deliveryResult = repository.getByOrder(orderId)) {
                    is ApiResult.Success -> {
                        val delivery = deliveryResult.data
                        _state.update {
                            it.copy(delivery = delivery, isLoading = false, errorMessage = null)
                        }
                        if (delivery.status.isTerminalDeliveryStatus()) break
                        if (delivery.driverId != null && delivery.status != "SEARCHING") {
                            loadLocation(delivery.deliveryId)
                        }
                    }
                    is ApiResult.Error -> _state.update {
                        it.copy(isLoading = false, isConnectionLost = true, errorMessage = deliveryResult.message)
                    }
                    ApiResult.Loading -> Unit
                }
                delay(pollIntervalMs)
            }
        }
    }

    private suspend fun loadLocation(deliveryId: String) {
        when (val locationResult = repository.getDriverLocation(deliveryId)) {
            is ApiResult.Success -> _state.update {
                it.copy(location = locationResult.data, isConnectionLost = false, errorMessage = null)
            }
            is ApiResult.Error -> _state.update {
                it.copy(isConnectionLost = true, errorMessage = locationResult.message)
            }
            ApiResult.Loading -> Unit
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun retry() {
        stopPolling()
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        startPolling()
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    class Factory(
        private val orderId: String,
        private val repository: DeliveryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TrackingViewModel::class.java))
            return TrackingViewModel(orderId, repository) as T
        }
    }
}
