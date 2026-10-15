package com.deliveryapp.customer.feature.order

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deliveryapp.customer.DeliveryCustomerApplication
import com.deliveryapp.customer.core.location.DeviceLocationProvider
import com.deliveryapp.customer.data.dto.AddressSuggestion
import com.deliveryapp.customer.data.dto.OrderResponse
import com.deliveryapp.customer.ui.components.AppSurface
import com.deliveryapp.customer.ui.components.CustomerBottomBar
import com.deliveryapp.customer.ui.components.DeliveryCard
import com.deliveryapp.customer.ui.components.InfoRow
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.components.ScreenHeader
import com.deliveryapp.customer.ui.components.SecondaryAction
import com.deliveryapp.customer.ui.components.StatusBanner
import com.deliveryapp.customer.ui.theme.DeliveryBorder
import com.deliveryapp.customer.ui.theme.DeliveryCanvas
import com.deliveryapp.customer.ui.theme.DeliveryDanger
import com.deliveryapp.customer.ui.theme.DeliveryGreen
import com.deliveryapp.customer.ui.theme.DeliveryGreenSoft
import com.deliveryapp.customer.ui.theme.DeliveryMuted
import com.deliveryapp.customer.ui.theme.DeliverySurface
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun CreateOrderScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateOrderViewModel = createOrderViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val readCurrentPickupLocation: () -> Unit = {
        viewModel.startLocatingPickup()
        scope.launch {
            val location = DeviceLocationProvider.getCurrentLocation(context)
            if (location == null) {
                viewModel.setPickupLocationError("Không lấy được GPS. Hãy bật vị trí rồi thử lại.")
            } else {
                viewModel.usePickupLocation(location.latitude, location.longitude)
            }
        }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) readCurrentPickupLocation()
        else viewModel.setPickupLocationError("Bạn cần cho phép quyền vị trí để dùng GPS.")
    }
    val requestPickupLocation: () -> Unit = {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            readCurrentPickupLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }
    LaunchedEffect(state.createdOrder?.orderId) {
        state.createdOrder?.orderId?.let {
            viewModel.consumeCreatedOrder()
            onCreated(it)
        }
    }

    AppSurface {
        Scaffold(
            topBar = { ScreenHeader("Tạo đơn giao hàng", onBack = onBack) },
            bottomBar = {
                Surface(color = DeliverySurface) {
                    PrimaryAction(
                        label = when {
                            state.isSubmitting -> "Đang tạo đơn…"
                            state.isPreviewing -> "Đang tính phí…"
                            state.preview == null -> "Xem phí và điểm sử dụng"
                            else -> "Xác nhận tạo đơn • ${money(state.preview!!.totalFee)}"
                        },
                        onClick = viewModel::submit,
                        modifier = Modifier.padding(20.dp),
                        loading = state.isSubmitting || state.isPreviewing
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { Spacer(Modifier.height(2.dp)) }
                item { ThreeStepProgress(1) }
                item { Text("Địa điểm giao nhận", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                item {
                    AddressAutocompleteField(
                        marker = "A",
                        label = "Điểm lấy hàng",
                        value = state.pickupAddress,
                        error = state.fieldErrors[CreateOrderField.PickupAddress],
                        suggestions = state.pickupSuggestions,
                        isSearching = state.isSearchingPickup,
                        highlighted = state.pickupLatitude.isNotBlank(),
                        onValueChange = { viewModel.onAddressQueryChanged(CreateOrderField.PickupAddress, it) },
                        onSuggestionSelected = { viewModel.selectAddress(CreateOrderField.PickupAddress, it) }
                    )
                    TextButton(onClick = requestPickupLocation, enabled = !state.isLocatingPickup, modifier = Modifier.fillMaxWidth()) {
                        if (state.isLocatingPickup) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.MyLocation, null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.isLocatingPickup) "Đang lấy vị trí…" else "Dùng vị trí hiện tại")
                    }
                    state.locationMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = DeliveryMuted) }
                }
                item {
                    AddressAutocompleteField(
                        marker = "B",
                        label = "Điểm giao hàng",
                        value = state.dropoffAddress,
                        error = state.fieldErrors[CreateOrderField.DropoffAddress],
                        suggestions = state.dropoffSuggestions,
                        isSearching = state.isSearchingDropoff,
                        highlighted = state.dropoffLatitude.isNotBlank(),
                        onValueChange = { viewModel.onAddressQueryChanged(CreateOrderField.DropoffAddress, it) },
                        onSuggestionSelected = { viewModel.selectAddress(CreateOrderField.DropoffAddress, it) }
                    )
                }
                item {
                    OrderField(
                        "Số điện thoại người nhận • bắt buộc",
                        state.receiverPhone,
                        state.fieldErrors[CreateOrderField.ReceiverPhone],
                        KeyboardType.Phone
                    ) { viewModel.updateField(CreateOrderField.ReceiverPhone, it) }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Chọn mã ưu đãi", fontWeight = FontWeight.Bold)
                        if (state.promotionsLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        state.promotionsError?.let {
                            Text(it, color = DeliveryDanger)
                            TextButton(onClick = viewModel::loadPromotions) { Text("Tải lại mã ưu đãi") }
                        }
                        FilterChip(selected = state.promotionCode.isBlank(),
                            onClick = { viewModel.updateField(CreateOrderField.PromotionCode, "") },
                            label = { Text("Không dùng mã") })
                        state.promotions.forEach { promotion ->
                            FilterChip(selected = state.promotionCode == promotion.code,
                                onClick = { viewModel.updateField(CreateOrderField.PromotionCode, promotion.code) },
                                label = { Column {
                                    Text("${promotion.code} • ${promotion.description}")
                                    Text("Đơn tối thiểu ${money(promotion.minOrderAmount)} • hết hạn ${promotion.endAt.take(10)}")
                                } })
                        }
                        if (!state.promotionsLoading && state.promotions.isEmpty() && state.promotionsError == null)
                            Text("Hiện chưa có mã ưu đãi còn hiệu lực.")
                        Box(Modifier.fillMaxWidth()) {
                            OrderField(
                                "Điểm sử dụng",
                                state.pointsToUse,
                                state.fieldErrors[CreateOrderField.PointsToUse],
                                KeyboardType.Number
                            ) { viewModel.updateField(CreateOrderField.PointsToUse, it) }
                        }
                    }
                }
                item {
                    Surface(color = DeliveryGreenSoft, shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Mã ưu đãi & điểm thưởng", style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
                                Text(
                                    listOfNotNull(
                                        state.promotionCode.ifBlank { null },
                                        state.pointsToUse.toIntOrNull()?.takeIf { it > 0 }?.let { "Dùng $it điểm" }
                                    ).joinToString(" • ").ifBlank { "Chưa áp dụng" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text("%", color = DeliveryGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                state.errorMessage?.let { item { ErrorCard(it) } }
                state.preview?.let { preview -> item {
                    DeliveryCard {
                        Text("Phí dự kiến", fontWeight = FontWeight.Bold)
                        InfoRow("Phí cơ bản", money(preview.price.baseFee))
                        InfoRow("Phụ phí", money(preview.price.surcharge))
                        InfoRow("Phí nền tảng", money(preview.price.platformFee))
                        InfoRow("Giảm khuyến mãi", money(preview.price.discountPromotion))
                        InfoRow("Điểm khả dụng", preview.points.availablePoints.toString())
                        InfoRow("Điểm có thể dùng", preview.points.usablePoints.toString())
                        InfoRow("Giảm bằng điểm", money(preview.points.discountAmount))
                        InfoRow("Tổng dự kiến", money(preview.totalFee))
                        Text("Ưu đãi và số dư được kiểm tra lại khi tạo đơn.", style = MaterialTheme.typography.bodySmall)
                        if (preview.points.usablePoints != state.pointsToUse.toIntOrNull()) {
                            TextButton(onClick = viewModel::useSuggestedPoints) {
                                Text("Dùng ${preview.points.usablePoints} điểm và tính lại")
                            }
                        }
                        TextButton(onClick = viewModel::previewPrice) { Text("Cập nhật phí") }
                    }
                } }
                item { Spacer(Modifier.height(10.dp)) }
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
    AppSurface {
        ScreenWithBottomBar("orders", onHome, {}, onRewards, onProfile) { contentModifier ->
            LazyColumn(contentModifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item { Spacer(Modifier.height(4.dp)) }
                item { Text("Đơn hàng của bạn", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OrderFilter.entries) { filter ->
                            FilterChip(
                                selected = state.filter == filter,
                                onClick = { viewModel.changeFilter(filter) },
                                label = { Text(filter.label) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeliveryGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = DeliveryCanvas,
                                    labelColor = DeliveryMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = state.filter == filter,
                                    borderColor = DeliveryBorder,
                                    selectedBorderColor = DeliveryGreen
                                )
                            )
                        }
                    }
                }
                if (state.isLoading) item { LoadingBlock("Đang tải danh sách đơn…") }
                else if (state.items.isEmpty() && state.errorMessage == null) item { EmptyCard("Chưa có đơn hàng ở trạng thái này.") }
                else items(state.items, key = { it.orderId }) { order -> OrderCard(order) { onOrderSelected(order.orderId) } }
                state.errorMessage?.let {
                    item {
                        ErrorCard(it)
                        TextButton(onClick = viewModel::refresh) { Text("Thử lại") }
                    }
                }
                if (state.hasMore) {
                    item {
                        SecondaryAction(
                            if (state.isLoadingMore) "Đang tải…" else "Tải thêm",
                            viewModel::loadMore,
                            enabled = !state.isLoadingMore
                        )
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
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
                    cancelReason,
                    { cancelReason = it },
                    label = { Text("Lý do hủy") },
                    enabled = !state.isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelOrder(cancelReason)
                        showCancelDialog = false
                    },
                    enabled = !state.isCancelling
                ) { Text("Xác nhận hủy", color = DeliveryDanger) }
            },
            dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("Không") } }
        )
    }

    AppSurface {
        Scaffold(topBar = { ScreenHeader("Chi tiết đơn hàng", "#${orderId.take(10)}", onBack) }) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(Modifier.height(2.dp)) }
                item { ThreeStepProgress(if (state.order?.status == "PENDING") 1 else 2) }
                if (state.isLoading && state.order == null) item { LoadingBlock("Đang tải chi tiết…") }
                state.order?.let { order ->
                    item {
                        StatusBanner(
                            statusLabel(order.status),
                            if (order.status == "PENDING") "Ứng dụng sẽ tự cập nhật sau vài giây" else "Trạng thái mới nhất của đơn hàng"
                        )
                    }
                    item { Text("Thông tin giao hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    item { OrderSummary(order) }
                    item { Text("Thanh toán", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    item { FeeBreakdown(order) }
                    state.matching?.let { matching ->
                        item {
                            InfoCard(
                                matchingLabel(matching.searchStatus),
                                "Bán kính ${matching.currentRadiusKm} km • đã mời ${matching.invitedDrivers} tài xế"
                            )
                        }
                    }
                    if (order.status in setOf("ASSIGNED", "PICKED_UP", "DELIVERING")) {
                        item { PrimaryAction("Theo dõi tài xế", { onTrack(order.orderId) }) }
                    }
                    if (order.status in setOf("PENDING", "ASSIGNED")) {
                        item {
                            SecondaryAction(
                                if (state.isCancelling) "Đang hủy…" else "Hủy đơn",
                                { showCancelDialog = true },
                                enabled = !state.isCancelling,
                                danger = true
                            )
                        }
                    }
                }
                state.infoMessage?.let { item { StatusBanner("Thành công", it) } }
                state.errorMessage?.let {
                    item {
                        ErrorCard(it)
                        TextButton(onClick = viewModel::loadOrder) { Text("Tải lại") }
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun ThreeStepProgress(activeStep: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) { index ->
            Box(
                Modifier.weight(if (index + 1 == activeStep) 1.65f else 1f).height(5.dp)
                    .background(if (index + 1 == activeStep) DeliveryGreen else DeliveryBorder, CircleShape)
            )
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value,
            onValueChange,
            label = { Text(label, fontSize = 12.sp) },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeliveryGreen,
                unfocusedBorderColor = DeliveryBorder,
                focusedContainerColor = DeliverySurface,
                unfocusedContainerColor = DeliverySurface
            )
        )
        error?.let { Text(it, color = DeliveryDanger, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun AddressAutocompleteField(
    marker: String,
    label: String,
    value: String,
    error: String?,
    suggestions: List<AddressSuggestion>,
    isSearching: Boolean,
    highlighted: Boolean,
    onValueChange: (String) -> Unit,
    onSuggestionSelected: (AddressSuggestion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value,
            onValueChange,
            label = { Text(label, fontSize = 12.sp) },
            placeholder = { Text("Nhập ít nhất 3 ký tự") },
            leadingIcon = {
                Surface(Modifier.size(40.dp), CircleShape, color = if (highlighted) DeliveryGreenSoft else DeliveryCanvas) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(marker, color = if (highlighted) DeliveryGreen else DeliveryMuted, fontWeight = FontWeight.Bold)
                    }
                }
            },
            trailingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = DeliveryMuted) },
            isError = error != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeliveryGreen,
                unfocusedBorderColor = if (highlighted) DeliveryGreen else DeliveryBorder,
                focusedContainerColor = DeliverySurface,
                unfocusedContainerColor = DeliverySurface
            )
        )
        error?.let { Text(it, color = DeliveryDanger, style = MaterialTheme.typography.bodySmall) }
        if (isSearching) LinearProgressIndicator(Modifier.fillMaxWidth(), color = DeliveryGreen)
        if (suggestions.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(DeliverySurface),
                border = BorderStroke(1.dp, DeliveryBorder)
            ) {
                Column {
                    suggestions.forEachIndexed { index, suggestion ->
                        Text(
                            suggestion.label,
                            Modifier.fillMaxWidth().clickable { onSuggestionSelected(suggestion) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (index != suggestions.lastIndex) HorizontalDivider(color = DeliveryBorder)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderResponse, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(DeliverySurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DeliveryBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("#${order.orderId.take(10)}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(color = DeliveryGreenSoft, shape = CircleShape) {
                    Text(
                        statusLabel(order.status),
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = DeliveryGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            RouteRow(order.pickupAddress, order.dropoffAddress)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(order.createdAt.take(16).replace('T', ' '), style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
                Spacer(Modifier.weight(1f))
                Text(money(order.totalFee), fontWeight = FontWeight.Bold)
            }
            if (order.status in setOf("PENDING", "ASSIGNED", "PICKED_UP", "DELIVERING")) {
                PrimaryAction(if (order.status == "PENDING") "Xem trạng thái tìm tài xế" else "Theo dõi đơn hàng", onClick)
            }
        }
    }
}

@Composable
private fun RouteRow(pickup: String, dropoff: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("●", color = DeliveryGreen, fontSize = 11.sp)
            Text("│", color = DeliveryBorder, fontSize = 12.sp)
            Text("○", color = DeliveryMuted, fontSize = 11.sp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(pickup, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(dropoff, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, color = DeliveryMuted)
        }
    }
}

@Composable
private fun OrderSummary(order: OrderResponse) {
    DeliveryCard {
        InfoRow("Điểm lấy", order.pickupAddress)
        InfoRow("Điểm giao", order.dropoffAddress)
        InfoRow("Khoảng cách", "${order.distanceKm} km")
        InfoRow("SĐT người nhận", order.receiverPhone)
        InfoRow("Trạng thái", order.status)
    }
}

@Composable
private fun FeeBreakdown(order: OrderResponse) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(DeliverySurface), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tổng phí  ${money(order.totalFee)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val discount = order.discountPromotion + order.discountPoints
            Text(
                if (discount > 0) "Đã giảm ${money(discount)} từ ưu đãi và điểm" else "Chưa áp dụng ưu đãi hoặc điểm",
                style = MaterialTheme.typography.bodySmall,
                color = DeliveryGreen
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFFFF2F0)), shape = RoundedCornerShape(16.dp)) {
        Text(message, color = DeliveryDanger, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun EmptyCard(message: String) {
    DeliveryCard { Text(message, color = DeliveryMuted) }
}

@Composable
private fun InfoCard(title: String, body: String) {
    DeliveryCard {
        Text(title, fontWeight = FontWeight.Bold)
        Text(body, color = DeliveryMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LoadingBlock(message: String) {
    Row(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = DeliveryGreen)
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
    Scaffold(bottomBar = { CustomerBottomBar(selected, onHome, onOrders, onRewards, onProfile) }) { padding ->
        content(Modifier.fillMaxSize().padding(padding))
    }
}

@Composable
private fun createOrderViewModel(): CreateOrderViewModel {
    val app = LocalContext.current.applicationContext as DeliveryCustomerApplication
    return viewModel(factory = CreateOrderViewModel.Factory(app.container.orderRepository, app.container.addressRepository, app.container.checkoutRepository))
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
        factory = OrderDetailViewModel.Factory(orderId, app.container.orderRepository, app.container.deliveryRepository)
    )
}

private fun money(amount: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount)

private fun statusLabel(status: String): String = when (status) {
    "PENDING" -> "Đang tìm tài xế"
    "ASSIGNED" -> "Đã có tài xế"
    "PICKED_UP" -> "Đang lấy hàng"
    "DELIVERING" -> "Đang giao"
    "COMPLETED" -> "Đã giao"
    "CANCELLED" -> "Đã hủy"
    "NO_DRIVER_FOUND" -> "Không có tài xế"
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
