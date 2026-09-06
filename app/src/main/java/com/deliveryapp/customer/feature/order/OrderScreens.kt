package com.deliveryapp.customer.feature.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deliveryapp.customer.DeliveryCustomerApplication
import com.deliveryapp.customer.data.dto.OrderResponse
import com.deliveryapp.customer.ui.components.CustomerBottomBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreateOrderScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateOrderViewModel = createOrderViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.createdOrder?.orderId) {
        state.createdOrder?.orderId?.let {
            viewModel.consumeCreatedOrder()
            onCreated(it)
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("Tạo đơn giao hàng", style = MaterialTheme.typography.headlineMedium)
                Text("Nhập thông tin lấy và giao hàng", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                OrderField(
                    label = "Địa chỉ lấy hàng",
                    value = state.pickupAddress,
                    error = state.fieldErrors[CreateOrderField.PickupAddress],
                    onValueChange = { viewModel.updateField(CreateOrderField.PickupAddress, it) }
                )
            }
            item {
                CoordinateRow(
                    latitude = state.pickupLatitude,
                    longitude = state.pickupLongitude,
                    latitudeError = state.fieldErrors[CreateOrderField.PickupLatitude],
                    longitudeError = state.fieldErrors[CreateOrderField.PickupLongitude],
                    onLatitudeChange = { viewModel.updateField(CreateOrderField.PickupLatitude, it) },
                    onLongitudeChange = { viewModel.updateField(CreateOrderField.PickupLongitude, it) }
                )
            }
            item {
                OrderField(
                    label = "Địa chỉ giao hàng",
                    value = state.dropoffAddress,
                    error = state.fieldErrors[CreateOrderField.DropoffAddress],
                    onValueChange = { viewModel.updateField(CreateOrderField.DropoffAddress, it) }
                )
            }
            item {
                CoordinateRow(
                    latitude = state.dropoffLatitude,
                    longitude = state.dropoffLongitude,
                    latitudeError = state.fieldErrors[CreateOrderField.DropoffLatitude],
                    longitudeError = state.fieldErrors[CreateOrderField.DropoffLongitude],
                    onLatitudeChange = { viewModel.updateField(CreateOrderField.DropoffLatitude, it) },
                    onLongitudeChange = { viewModel.updateField(CreateOrderField.DropoffLongitude, it) }
                )
            }
            item {
                OrderField(
                    label = "Số điện thoại người nhận",
                    value = state.receiverPhone,
                    error = state.fieldErrors[CreateOrderField.ReceiverPhone],
                    keyboardType = KeyboardType.Phone,
                    onValueChange = { viewModel.updateField(CreateOrderField.ReceiverPhone, it) }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        OrderField(
                            label = "Mã ưu đãi",
                            value = state.promotionCode,
                            error = state.fieldErrors[CreateOrderField.PromotionCode],
                            onValueChange = { viewModel.updateField(CreateOrderField.PromotionCode, it) }
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        OrderField(
                            label = "Điểm sử dụng",
                            value = state.pointsToUse,
                            error = state.fieldErrors[CreateOrderField.PointsToUse],
                            keyboardType = KeyboardType.Number,
                            onValueChange = { viewModel.updateField(CreateOrderField.PointsToUse, it) }
                        )
                    }
                }
            }
            item {
                Text(
                    text = "Mã yêu cầu: ${state.clientRequestId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.errorMessage?.let { message -> item { ErrorCard(message) } }
            item {
                Button(
                    onClick = viewModel::submit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (state.isSubmitting) "Đang tạo đơn…" else "Xác nhận tạo đơn")
                }
            }
            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Quay lại")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun OrdersScreen(
    onOrderSelected: (String) -> Unit,
    onHome: () -> Unit,
    onRewards: () -> Unit,
    onProfile: () -> Unit,
    viewModel: OrdersViewModel = ordersViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenWithBottomBar(
        selected = "orders",
        onHome = onHome,
        onOrders = {},
        onRewards = onRewards,
        onProfile = onProfile
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("Đơn hàng của bạn", style = MaterialTheme.typography.headlineMedium)
                Text("Theo dõi tất cả đơn theo trạng thái", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(OrderFilter.entries) { filter ->
                        FilterChip(
                            selected = state.filter == filter,
                            onClick = { viewModel.changeFilter(filter) },
                            label = { Text(filter.label) }
                        )
                    }
                }
            }
            if (state.isLoading) {
                item { LoadingBlock("Đang tải danh sách đơn…") }
            } else if (state.items.isEmpty() && state.errorMessage == null) {
                item { EmptyCard("Chưa có đơn hàng ở trạng thái này.") }
            } else {
                items(state.items, key = { it.orderId }) { order ->
                    OrderCard(order = order, onClick = { onOrderSelected(order.orderId) })
                }
            }
            state.errorMessage?.let { message ->
                item {
                    ErrorCard(message)
                    TextButton(onClick = viewModel::refresh) { Text("Thử lại") }
                }
            }
            if (state.hasMore) {
                item {
                    OutlinedButton(
                        onClick = viewModel::loadMore,
                        enabled = !state.isLoadingMore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isLoadingMore) "Đang tải…" else "Tải thêm")
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onTrack: (String) -> Unit,
    viewModel: OrderDetailViewModel = orderDetailViewModel(orderId)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("CUSTOMER_CHANGED_MIND") }

    DisposableEffect(orderId) {
        viewModel.startMatchingPolling()
        onDispose(viewModel::stopMatchingPolling)
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isCancelling) showCancelDialog = false },
            title = { Text("Hủy đơn hàng") },
            text = {
                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = { cancelReason = it },
                    label = { Text("Lý do hủy") },
                    enabled = !state.isCancelling,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelOrder(cancelReason)
                        showCancelDialog = false
                    },
                    enabled = !state.isCancelling
                ) { Text("Xác nhận hủy") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Không") }
            }
        )
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("Chi tiết đơn hàng", style = MaterialTheme.typography.headlineMedium)
                Text("Mã đơn: $orderId", style = MaterialTheme.typography.bodySmall)
            }
            if (state.isLoading && state.order == null) {
                item { LoadingBlock("Đang tải chi tiết…") }
            }
            state.order?.let { order ->
                item { OrderSummary(order) }
                item { FeeBreakdown(order) }
                state.matching?.let { matching ->
                    item {
                        InfoCard(
                            title = matchingLabel(matching.searchStatus),
                            body = "Bán kính ${matching.currentRadiusKm} km · đã mời ${matching.invitedDrivers} tài xế"
                        )
                    }
                }
                if (order.status in setOf("ASSIGNED", "PICKED_UP", "DELIVERING")) {
                    item {
                        Button(onClick = { onTrack(order.orderId) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Theo dõi tài xế")
                        }
                    }
                }
                if (order.status in setOf("PENDING", "ASSIGNED")) {
                    item {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            enabled = !state.isCancelling,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (state.isCancelling) "Đang hủy…" else "Hủy đơn")
                        }
                    }
                }
            }
            state.infoMessage?.let { message -> item { InfoCard("Thành công", message) } }
            state.errorMessage?.let { message ->
                item {
                    ErrorCard(message)
                    TextButton(onClick = viewModel::loadOrder) { Text("Tải lại") }
                }
            }
            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Về danh sách đơn")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun OrderField(
    label: String,
    value: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CoordinateRow(
    latitude: String,
    longitude: String,
    latitudeError: String?,
    longitudeError: String?,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) {
            OrderField("Vĩ độ", latitude, latitudeError, KeyboardType.Decimal, onLatitudeChange)
        }
        Box(Modifier.weight(1f)) {
            OrderField("Kinh độ", longitude, longitudeError, KeyboardType.Decimal, onLongitudeChange)
        }
    }
}

@Composable
private fun OrderCard(order: OrderResponse, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("#${order.orderId.take(8)}", fontWeight = FontWeight.Bold)
                Text(statusLabel(order.status), color = MaterialTheme.colorScheme.primary)
            }
            Text("${order.pickupAddress} → ${order.dropoffAddress}")
            Text(money(order.totalFee), style = MaterialTheme.typography.titleMedium)
            Text(order.createdAt.take(19).replace('T', ' '), style = MaterialTheme.typography.bodySmall)
            if (order.status in setOf("PENDING", "ASSIGNED", "PICKED_UP", "DELIVERING")) {
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text(if (order.status == "PENDING") "Xem trạng thái tìm tài xế" else "Theo dõi đơn hàng")
                }
            } else {
                OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Xem chi tiết")
                }
            }
        }
    }
}

@Composable
private fun OrderSummary(order: OrderResponse) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(statusLabel(order.status), style = MaterialTheme.typography.titleLarge)
            Text("Lấy: ${order.pickupAddress}")
            Text("Giao: ${order.dropoffAddress}")
            Text("Người nhận: ${order.receiverPhone}")
            Text("Quãng đường: ${order.distanceKm} km")
        }
    }
}

@Composable
private fun FeeBreakdown(order: OrderResponse) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Chi tiết phí", style = MaterialTheme.typography.titleMedium)
            FeeRow("Phí cơ bản", order.baseFee)
            FeeRow("Phụ phí", order.surcharge)
            FeeRow("Phí nền tảng", order.platformFee)
            FeeRow("Giảm khuyến mãi", -order.discountPromotion)
            FeeRow("Giảm từ điểm", -order.discountPoints)
            HorizontalDivider()
            FeeRow("Tổng cộng", order.totalFee, emphasized = true)
        }
    }
}

@Composable
private fun FeeRow(label: String, amount: Long, emphasized: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal)
        Text(money(amount), fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(Modifier.fillMaxWidth()) {
        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(Modifier.fillMaxWidth()) {
        Text(message, modifier = Modifier.padding(24.dp))
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body)
        }
    }
}

@Composable
private fun LoadingBlock(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.width(24.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp))
        Text(message)
    }
}

@Composable
private fun ScreenWithBottomBar(
    selected: String,
    onHome: () -> Unit,
    onOrders: () -> Unit,
    onRewards: () -> Unit,
    onProfile: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        bottomBar = { CustomerBottomBar(selected, onHome, onOrders, onRewards, onProfile) }
    ) { padding -> content(Modifier.fillMaxSize().padding(padding)) }
}

@Composable
private fun createOrderViewModel(): CreateOrderViewModel {
    val app = LocalContext.current.applicationContext as DeliveryCustomerApplication
    return viewModel(factory = CreateOrderViewModel.Factory(app.container.orderRepository))
}

@Composable
private fun ordersViewModel(): OrdersViewModel {
    val app = LocalContext.current.applicationContext as DeliveryCustomerApplication
    return viewModel(factory = OrdersViewModel.Factory(app.container.orderRepository))
}

@Composable
private fun orderDetailViewModel(orderId: String): OrderDetailViewModel {
    val app = LocalContext.current.applicationContext as DeliveryCustomerApplication
    return viewModel(
        key = "order-detail-$orderId",
        factory = OrderDetailViewModel.Factory(
            orderId,
            app.container.orderRepository,
            app.container.deliveryRepository
        )
    )
}

private fun money(amount: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount)

private fun statusLabel(status: String): String = when (status) {
    "PENDING" -> "Đang tìm tài xế"
    "ASSIGNED" -> "Đã có tài xế"
    "PICKED_UP" -> "Đã lấy hàng"
    "DELIVERING" -> "Đang giao"
    "COMPLETED" -> "Hoàn tất"
    "CANCELLED" -> "Đã hủy"
    "NO_DRIVER_FOUND" -> "Không tìm thấy tài xế"
    else -> status
}

private fun matchingLabel(status: String): String = when (status) {
    "NOT_STARTED" -> "Chuẩn bị tìm tài xế"
    "SEARCHING" -> "Đang tìm tài xế gần bạn"
    "MATCHED" -> "Đã tìm thấy tài xế"
    "NO_DRIVER_FOUND" -> "Chưa tìm thấy tài xế"
    "CANCELLED" -> "Đã dừng tìm tài xế"
    else -> status
}
