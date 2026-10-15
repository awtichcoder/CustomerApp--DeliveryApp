package com.deliveryapp.customer.feature.tracking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deliveryapp.customer.DeliveryCustomerApplication
import com.deliveryapp.customer.data.dto.RoutePoint
import com.deliveryapp.customer.ui.theme.DeliveryBorder
import com.deliveryapp.customer.ui.theme.DeliveryGreen
import com.deliveryapp.customer.ui.theme.DeliveryGreenSoft
import com.deliveryapp.customer.ui.theme.DeliveryMuted
import com.deliveryapp.customer.ui.theme.DeliverySurface
import com.deliveryapp.customer.ui.theme.DeliveryText
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun TrackingScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: TrackingViewModel = trackingViewModel(orderId)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showHelp by remember { mutableStateOf(false) }
    if (showHelp) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Trợ giúp theo dõi đơn") },
            text = { Text("Vị trí tài xế được cập nhật khoảng 30 giây/lần. Nếu chưa thấy vị trí, hãy kiểm tra kết nối rồi chọn Kết nối lại. Bạn có thể hủy đơn ở trang chi tiết trước khi tài xế lấy hàng. Mã đơn: $orderId") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { showHelp = false; viewModel.retry() }) { Text("Kết nối lại") } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showHelp = false; onBack() }) { Text("Chi tiết đơn") } }
        )
    }
    DisposableEffect(orderId) {
        viewModel.startPolling()
        onDispose(viewModel::stopPolling)
    }

    Scaffold(containerColor = DeliveryGreenSoft) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(Modifier.fillMaxWidth().weight(1.05f)) {
                TrackingMap(
                    latitude = state.location?.latitude,
                    longitude = state.location?.longitude,
                    routePoints = state.route?.points.orEmpty(),
                    destinationLabel = state.routeDestinationLabel,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = CircleShape, color = DeliverySurface) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, "Quay lại", tint = DeliveryText)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Đơn #${orderId.take(10)}", fontWeight = FontWeight.Bold)
                        Text(
                            "${state.delivery?.status ?: "Đang kết nối"} • cập nhật ${state.location?.recordedAt?.take(16)?.replace('T', ' ') ?: "—"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeliveryMuted
                        )
                    }
                    Surface(shape = CircleShape, color = DeliverySurface) {
                        IconButton(onClick = { showHelp = true }) {
                            Icon(Icons.Outlined.HelpOutline, "Trợ giúp", tint = DeliveryGreen)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = DeliverySurface
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            if (state.location == null) "Đang chờ GPS" else "GPS trực tiếp",
                            color = DeliveryGreen,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.route?.let { "%.1f km • %.0f phút".format(it.distanceKm, it.durationMinutes) }
                                ?: "Cập nhật mỗi 30 giây",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeliveryMuted
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                color = DeliverySurface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(width = 44.dp, height = 4.dp).background(DeliveryBorder, CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text("Theo dõi vị trí tài xế", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(50.dp).background(DeliveryGreenSoft, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.LocalShipping, null, tint = DeliveryGreen, modifier = Modifier.size(28.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(deliveryStatusLabel(state.delivery?.status), fontWeight = FontWeight.Bold)
                            Text(
                                state.delivery?.driverId?.let { "Mã tài xế: ${it.take(8)}" } ?: "Đang tìm tài xế phù hợp",
                                style = MaterialTheme.typography.bodySmall,
                                color = DeliveryMuted
                            )
                        }
                        if (state.isLoading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                    HorizontalDivider(color = DeliveryBorder)
                    if (state.isRouteLoading) {
                        Text("Đang tìm tuyến đường…", color = DeliveryGreen, style = MaterialTheme.typography.bodySmall)
                    }
                    DeliveryTimeline(state.delivery)
                    if (state.isConnectionLost) {
                        Text("Mất kết nối tạm thời", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text(state.errorMessage ?: "Chưa nhận được vị trí mới từ tài xế.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = viewModel::retry) { Text("Kết nối lại") }
                    }
                    state.routeErrorMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = DeliveryMuted) }
                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Về chi tiết đơn") }
                }
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
private fun TrackingMap(
    latitude: Double?,
    longitude: Double?,
    routePoints: List<RoutePoint>,
    destinationLabel: String?,
    modifier: Modifier = Modifier
) {
    val driverPosition = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val driverIcon = remember(context) { createDriverVehicleIcon(context) }
    val mapView = remember {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply { onCreate(null) }
    }
    var map by remember(mapView) { mutableStateOf<MapLibreMap?>(null) }
    var driverMarker by remember(mapView) { mutableStateOf<Marker?>(null) }
    var destinationMarker by remember(mapView) { mutableStateOf<Marker?>(null) }
    var routeLine by remember(mapView) { mutableStateOf<Polyline?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        val lifecycle = lifecycleOwner.lifecycle
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(map, driverPosition, routePoints, destinationLabel) {
        val readyMap = map ?: return@LaunchedEffect
        driverMarker?.let(readyMap::removeMarker)
        destinationMarker?.let(readyMap::removeMarker)
        routeLine?.let(readyMap::removePolyline)
        driverMarker = null
        destinationMarker = null
        routeLine = null
        if (driverPosition == null) {
            readyMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(DEFAULT_MAP_CENTER, 12.0),
                500
            )
            return@LaunchedEffect
        }
        driverMarker = readyMap.addMarker(
            MarkerOptions()
                .position(driverPosition)
                .title("Tài xế")
                .snippet("Vị trí cập nhật gần nhất")
                .icon(driverIcon)
        )
        if (routePoints.size >= 2) {
            val mapRoutePoints = routePoints.map { LatLng(it.latitude, it.longitude) }
            routeLine = readyMap.addPolyline(
                PolylineOptions()
                    .addAll(mapRoutePoints)
                    .color(Color.rgb(22, 163, 106))
                    .width(8f)
            )
            destinationMarker = readyMap.addMarker(
                MarkerOptions()
                    .position(mapRoutePoints.last())
                    .title(destinationLabel ?: "Điểm đến")
            )
            val routeBounds = LatLngBounds.Builder().includes(mapRoutePoints).build()
            val cameraPadding = (48 * context.resources.displayMetrics.density).toInt()
            readyMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(routeBounds, cameraPadding),
                700
            )
            return@LaunchedEffect
        }
        readyMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(driverPosition, if (routePoints.isEmpty()) 16.0 else 13.5),
            700
        )
    }

    Box(modifier = modifier.background(DeliveryGreenSoft)) {
            AndroidView(
                factory = {
                    mapView.apply {
                        getMapAsync { readyMap ->
                            readyMap.uiSettings.isCompassEnabled = true
                            readyMap.setStyle(MAP_STYLE_URL) {
                                map = readyMap
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "© OpenStreetMap contributors",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
            if (driverPosition == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Đang chờ vị trí tài xế", fontWeight = FontWeight.Bold)
                        Text(
                            "Bản đồ vẫn sẵn sàng và sẽ tự cập nhật sau khoảng 30 giây.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
    }
}

private fun createDriverVehicleIcon(context: Context): Icon {
    val size = (52 * context.resources.displayMetrics.density).toInt().coerceAtLeast(52)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val unit = size.toFloat()

    paint.color = Color.WHITE
    canvas.drawCircle(unit * 0.5f, unit * 0.5f, unit * 0.47f, paint)
    paint.color = Color.rgb(22, 163, 106)
    canvas.drawRoundRect(
        RectF(unit * 0.12f, unit * 0.38f, unit * 0.66f, unit * 0.70f),
        unit * 0.08f,
        unit * 0.08f,
        paint
    )
    canvas.drawRoundRect(
        RectF(unit * 0.58f, unit * 0.29f, unit * 0.86f, unit * 0.70f),
        unit * 0.07f,
        unit * 0.07f,
        paint
    )
    paint.color = Color.rgb(207, 244, 232)
    canvas.drawRoundRect(
        RectF(unit * 0.64f, unit * 0.35f, unit * 0.80f, unit * 0.50f),
        unit * 0.025f,
        unit * 0.025f,
        paint
    )
    paint.color = Color.rgb(255, 203, 69)
    canvas.drawRoundRect(
        RectF(unit * 0.25f, unit * 0.46f, unit * 0.49f, unit * 0.62f),
        unit * 0.025f,
        unit * 0.025f,
        paint
    )
    paint.color = Color.rgb(30, 52, 44)
    canvas.drawCircle(unit * 0.30f, unit * 0.72f, unit * 0.09f, paint)
    canvas.drawCircle(unit * 0.72f, unit * 0.72f, unit * 0.09f, paint)
    paint.color = Color.WHITE
    canvas.drawCircle(unit * 0.30f, unit * 0.72f, unit * 0.035f, paint)
    canvas.drawCircle(unit * 0.72f, unit * 0.72f, unit * 0.035f, paint)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}

@Composable
private fun trackingViewModel(orderId: String): TrackingViewModel {
    val app = LocalContext.current.applicationContext as DeliveryCustomerApplication
    return viewModel(
        key = "tracking-$orderId",
        factory = TrackingViewModel.Factory(
            orderId,
            app.container.deliveryRepository,
            app.container.routingRepository
        )
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

private val DEFAULT_MAP_CENTER = LatLng(10.7769, 106.7008)
private const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
