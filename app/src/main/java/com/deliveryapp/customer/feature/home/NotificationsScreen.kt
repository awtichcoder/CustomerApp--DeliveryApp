package com.deliveryapp.customer.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deliveryapp.customer.feature.order.OrdersViewModel
import com.deliveryapp.customer.ui.components.ScreenHeader

@Composable
fun NotificationsScreen(viewModel: OrdersViewModel, onBack: () -> Unit, onOrder: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { ScreenHeader("Cập nhật đơn hàng", onBack = onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Trạng thái mới nhất của các đơn. Chọn một đơn để xem chi tiết.")
                TextButton(onClick = viewModel::refresh, enabled = !state.isLoading) { Text("Làm mới") }
            }
            if (state.isLoading) item { CircularProgressIndicator() }
            state.errorMessage?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (!state.isLoading && state.items.isEmpty() && state.errorMessage == null)
                item { Text("Chưa có cập nhật đơn hàng.") }
            items(state.items, key = { it.orderId }) { order ->
                Card(Modifier.fillMaxWidth().clickable { onOrder(order.orderId) }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Đơn #${order.orderId.take(8)}")
                        Text(when (order.status) {
                            "PENDING" -> "Đang tìm tài xế"
                            "ASSIGNED" -> "Tài xế đã nhận đơn"
                            "PICKED_UP" -> "Tài xế đã lấy hàng"
                            "DELIVERING" -> "Đơn hàng đang được giao"
                            "COMPLETED" -> "Giao hàng thành công"
                            "CANCELLED" -> "Đơn đã hủy"
                            "NO_DRIVER_FOUND" -> "Chưa tìm được tài xế. Bạn có thể tạo đơn mới."
                            else -> order.status
                        })
                        Text(order.dropoffAddress, style = MaterialTheme.typography.bodySmall)
                        Text(order.updatedAt ?: order.createdAt, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (state.hasMore) item {
                TextButton(onClick = viewModel::loadMore, enabled = !state.isLoadingMore) { Text("Xem thêm") }
            }
        }
    }
}
