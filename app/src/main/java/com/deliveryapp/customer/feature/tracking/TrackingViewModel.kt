package com.deliveryapp.customer.feature.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.DeliveryResponse
import com.deliveryapp.customer.data.dto.DriverLocationResponse
import com.deliveryapp.customer.data.dto.RoutePath
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
    val route: RoutePath? = null,
    val routeDestinationLabel: String? = null,
    val isRouteLoading: Boolean = false,
    val routeErrorMessage: String? = null,
    val isLoading: Boolean = true,
    val isConnectionLost: Boolean = false,
    val errorMessage: String? = null
)

class TrackingViewModel(
    private val orderId: String,
    private val repository: DeliveryRepository,
    private val pollIntervalMs: Long = 30_000L,
    private val routingRepository: RoutingRepository = EmptyRoutingRepository
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
                            loadLocation(delivery)
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

    private suspend fun loadLocation(delivery: DeliveryResponse) {
        when (val locationResult = repository.getDriverLocation(delivery.deliveryId)) {
            is ApiResult.Success -> {
                _state.update {
                    it.copy(location = locationResult.data, isConnectionLost = false, errorMessage = null)
                }
                loadRoute(delivery, locationResult.data)
            }
            is ApiResult.Error -> _state.update {
                it.copy(isConnectionLost = true, errorMessage = locationResult.message)
            }
            ApiResult.Loading -> Unit
        }
    }

    private suspend fun loadRoute(delivery: DeliveryResponse, location: DriverLocationResponse) {
        val goingToDropoff = delivery.status == "PICKED_UP" || delivery.status == "DELIVERING"
        val targetLatitude = if (goingToDropoff) delivery.dropoffLatitude else delivery.pickupLatitude
        val targetLongitude = if (goingToDropoff) delivery.dropoffLongitude else delivery.pickupLongitude
        val targetLabel = if (goingToDropoff) "Đường đến điểm giao" else "Đường đến điểm lấy hàng"
        if (targetLatitude == 0.0 && targetLongitude == 0.0) {
            _state.update {
                it.copy(
                    route = null,
                    routeDestinationLabel = targetLabel,
                    isRouteLoading = false,
                    routeErrorMessage = "Backend chưa trả tọa độ của điểm đến."
                )
            }
            return
        }

        _state.update {
            it.copy(
                route = null,
                routeDestinationLabel = targetLabel,
                isRouteLoading = true,
                routeErrorMessage = null
            )
        }
        when (
            val result = routingRepository.getRoute(
                startLatitude = location.latitude,
                startLongitude = location.longitude,
                endLatitude = targetLatitude,
                endLongitude = targetLongitude
            )
        ) {
            is ApiResult.Success -> _state.update {
                it.copy(route = result.data, isRouteLoading = false, routeErrorMessage = null)
            }
            is ApiResult.Error -> _state.update {
                it.copy(route = null, isRouteLoading = false, routeErrorMessage = result.message)
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
        private val repository: DeliveryRepository,
        private val routingRepository: RoutingRepository = EmptyRoutingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TrackingViewModel::class.java))
            return TrackingViewModel(
                orderId = orderId,
                repository = repository,
                routingRepository = routingRepository
            ) as T
        }
    }
}
