package com.deliveryapp.customer.feature.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deliveryapp.customer.DeliveryCustomerApplication

@Composable
fun TrackingScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: TrackingViewModel = trackingViewModel(orderId)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DisposableEffect(orderId) {
        viewModel.startPolling()
        onDispose(viewModel::stopPolling)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Theo dõi giao hàng", style = MaterialTheme.typography.headlineMedium)
            Text("Mã đơn: $orderId", style = MaterialTheme.typography.bodySmall)

            if (state.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text("  Đang kết nối với tài xế…")
                }
            }

            TrackingMapPlaceholder(
                latitude = state.location?.latitude,
                longitude = state.location?.longitude
            )

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Trạng thái", style = MaterialTheme.typography.titleMedium)
                    Text(deliveryStatusLabel(state.delivery?.status), fontWeight = FontWeight.Bold)
                    state.delivery?.driverId?.let { Text("Tài xế: ${it.take(8)}…") }
                    state.location?.let {
                        Text("Vĩ độ: ${it.latitude}")
                        Text("Kinh độ: ${it.longitude}")
                        Text("Cập nhật: ${it.recordedAt.take(19).replace('T', ' ')}")
                        it.accuracyM?.let { accuracy -> Text("Độ chính xác: ${accuracy} m") }
                    }
                    DeliveryTimeline(state.delivery)
                }
            }

            if (state.isConnectionLost) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mất kết nối tạm thời", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text(state.errorMessage ?: "Chưa nhận được vị trí mới từ tài xế.")
                        Button(onClick = viewModel::retry) { Text("Kết nối lại") }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Về chi tiết đơn")
            }
        }
    }
}

@Composable
private fun DeliveryTimeline(delivery: com.deliveryapp.customer.data.dto.DeliveryResponse?) {
    val currentStatus = delivery?.status ?: "SEARCHING"
    val steps = listOf(
        "ASSIGNED" to (delivery?.assignedAt ?: ""),
        "PICKED_UP" to (delivery?.pickedUpAt ?: ""),
        "DELIVERING" to (delivery?.deliveringAt ?: ""),
        "COMPLETED" to (delivery?.completedAt ?: "")
    )
    val currentIndex = steps.indexOfFirst { it.first == currentStatus }
    Column(
        modifier = Modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Tiến trình giao hàng", style = MaterialTheme.typography.titleMedium)
        steps.forEachIndexed { index, (status, time) ->
            val reached = currentStatus == "COMPLETED" || (currentIndex >= 0 && index <= currentIndex)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(
                            if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                )
                Column(Modifier.padding(start = 10.dp)) {
                    Text(
                        deliveryStatusLabel(status),
                        color = if (status == currentStatus) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (status == currentStatus) FontWeight.Bold else FontWeight.Normal
                    )
                    if (time.isNotBlank()) {
                        Text(time.take(19).replace('T', ' '), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingMapPlaceholder(latitude: Double?, longitude: Double?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(Modifier.height(10.dp))
            if (latitude != null && longitude != null) {
                Text("Vị trí mới nhất")
                Text("$latitude, $longitude", fontWeight = FontWeight.Bold)
            } else {
                Text("Đang chờ vị trí tài xế")
            }
            Text(
                "Bản đồ Google Maps sẽ được bật khi nhóm cung cấp API key.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun trackingViewModel(orderId: String): TrackingViewModel {
    val app = LocalContext.current.applicationContext as DeliveryCustomerApplication
    return viewModel(
        key = "tracking-$orderId",
        factory = TrackingViewModel.Factory(orderId, app.container.deliveryRepository)
    )
}

private fun deliveryStatusLabel(status: String?): String = when (status) {
    null, "SEARCHING" -> "Đang tìm tài xế"
    "ASSIGNED" -> "Tài xế đang đến điểm lấy hàng"
    "PICKED_UP" -> "Tài xế đã lấy hàng"
    "DELIVERING" -> "Đang giao đến người nhận"
    "COMPLETED" -> "Giao hàng hoàn tất"
    "CANCELLED" -> "Đơn giao đã hủy"
    "NO_DRIVER_FOUND" -> "Không tìm thấy tài xế"
    else -> status
}
