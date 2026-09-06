package com.deliveryapp.customer.feature.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.dto.OrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OrderFilter(val label: String, val apiValue: String?) {
    All("Tất cả", null),
    Pending("Đang tìm", "PENDING"),
    Assigned("Đã nhận", "ASSIGNED"),
    PickedUp("Đã lấy", "PICKED_UP"),
    Delivering("Đang giao", "DELIVERING"),
    Completed("Hoàn tất", "COMPLETED"),
    Cancelled("Đã hủy", "CANCELLED"),
    NoDriver("Không có tài xế", "NO_DRIVER_FOUND")
}

data class OrdersUiState(
    val items: List<OrderResponse> = emptyList(),
    val filter: OrderFilter = OrderFilter.All,
    val page: Int = 0,
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
) {
    val hasMore: Boolean get() = items.size < totalItems
}

class OrdersViewModel(private val repository: OrderRepository) : ViewModel() {
    private val pageSize = 20
    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()

    init {
        load(reset = true)
    }

    fun refresh() = load(reset = true)

    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        load(reset = false)
    }

    fun changeFilter(filter: OrderFilter) {
        if (_state.value.filter == filter) return
        _state.update { it.copy(filter = filter) }
        load(reset = true)
    }

    private fun load(reset: Boolean) {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore) return
        val targetPage = if (reset) 1 else current.page + 1
        _state.update {
            it.copy(
                isLoading = reset,
                isLoadingMore = !reset,
                errorMessage = null,
                items = if (reset) emptyList() else it.items,
                page = if (reset) 0 else it.page,
                totalItems = if (reset) 0 else it.totalItems
            )
        }
        viewModelScope.launch {
            when (val result = repository.getOrders(targetPage, pageSize, _state.value.filter.apiValue)) {
                is ApiResult.Success -> _state.update { state ->
                    val combined = if (reset) result.data.items else state.items + result.data.items
                    state.copy(
                        items = combined.distinctBy { it.orderId },
                        page = result.data.page,
                        totalItems = result.data.totalItems,
                        isLoading = false,
                        isLoadingMore = false
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, isLoadingMore = false, errorMessage = result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    class Factory(private val repository: OrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(OrdersViewModel::class.java))
            return OrdersViewModel(repository) as T
        }
    }
}
