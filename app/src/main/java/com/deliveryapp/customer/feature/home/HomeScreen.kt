package com.deliveryapp.customer.feature.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliveryapp.customer.ui.components.AppSurface
import com.deliveryapp.customer.ui.components.CustomerBottomBar
import com.deliveryapp.customer.ui.components.ErrorState
import com.deliveryapp.customer.ui.components.LoadingState
import com.deliveryapp.customer.ui.components.SessionExpiredDialog
import com.deliveryapp.customer.ui.theme.DeliveryBorder
import com.deliveryapp.customer.ui.theme.DeliveryCanvas
import com.deliveryapp.customer.ui.theme.DeliveryGreen
import com.deliveryapp.customer.ui.theme.DeliveryGreenSoft
import com.deliveryapp.customer.ui.theme.DeliveryMuted
import com.deliveryapp.customer.ui.theme.DeliverySurface
import com.deliveryapp.customer.ui.theme.DeliveryText

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateOrder: () -> Unit,
    onOrders: () -> Unit,
    onRewards: () -> Unit,
    onProfile: () -> Unit,
    onSessionExpired: () -> Unit,
    onNotifications: () -> Unit
) {
    val state = viewModel.state
    if (state.sessionExpired) {
        SessionExpiredDialog {
            viewModel.consumeSessionExpired()
            onSessionExpired()
        }
    }
    AppSurface {
        Scaffold(bottomBar = { CustomerBottomBar("home", {}, onOrders, onRewards, onProfile) }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                HomeHeader(state.fullName, onNotifications)
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    when {
                        state.isLoading -> LoadingState("Đang tải hồ sơ")
                        state.errorMessage != null -> ErrorState(state.errorMessage, viewModel::load)
                        else -> HomeContent(onCreateOrder, onOrders, onRewards)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(fullName: String, onNotifications: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(104.dp).background(DeliverySurface).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(44.dp).background(DeliveryGreenSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PersonOutline, null, tint = DeliveryGreen, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Chào buổi sáng, ${fullName.substringAfterLast(' ')} 👋",
                style = MaterialTheme.typography.bodySmall,
                color = DeliveryMuted
            )
            Text("Bạn muốn giao gì hôm nay?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Surface(shape = CircleShape, color = DeliveryGreenSoft) {
            IconButton(onClick = onNotifications) {
                Icon(Icons.Outlined.NotificationsNone, "Thông báo", tint = DeliveryGreen)
            }
        }
    }
}

@Composable
private fun HomeContent(onCreateOrder: () -> Unit, onOrders: () -> Unit, onRewards: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onCreateOrder),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(DeliverySurface),
        border = BorderStroke(1.dp, DeliveryBorder)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(DeliveryGreenSoft, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.LocationOn, null, tint = DeliveryGreen)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Địa chỉ lấy hàng", style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
                Text("Dùng GPS hoặc nhập địa chỉ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = DeliveryMuted)
        }
    }

    Text("Chức năng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ServiceCard(
            "Tạo đơn",
            "Giao hàng mới",
            Icons.Outlined.AddBox,
            true,
            Modifier.weight(1f),
            onCreateOrder
        )
        ServiceCard(
            "Đơn hàng",
            "Theo dõi đơn",
            Icons.AutoMirrored.Outlined.ReceiptLong,
            false,
            Modifier.weight(1f),
            onOrders
        )
        ServiceCard("Điểm", "Xem ưu đãi", Icons.Outlined.CardGiftcard, false, Modifier.weight(1f), onRewards)
    }

    Card(
        Modifier.fillMaxWidth().height(118.dp).clickable(onClick = onCreateOrder),
        colors = CardDefaults.cardColors(DeliveryGreen),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.fillMaxSize().padding(18.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Mã khuyến mãi", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("Nhập mã khi tạo đơn", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Ví dụ: FREESHIP", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
            Text("🎁", fontSize = 38.sp)
        }
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Đơn gần đây", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text(
            "Xem tất cả",
            modifier = Modifier.clickable(onClick = onOrders).padding(6.dp),
            color = DeliveryGreen,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onOrders),
        colors = CardDefaults.cardColors(DeliverySurface),
        border = BorderStroke(1.dp, DeliveryBorder),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("Đơn đang hoạt động", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(color = DeliveryGreenSoft, shape = CircleShape) {
                    Text("Mở danh sách", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = DeliveryGreen, fontSize = 11.sp)
                }
            }
            Text("Xem đơn mới nhất và vị trí tài xế", color = DeliveryMuted, style = MaterialTheme.typography.bodyMedium)
            Text("Chạm để kiểm tra trạng thái", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ServiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    highlighted: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier.height(126.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(if (highlighted) DeliveryGreenSoft else DeliverySurface),
        border = BorderStroke(1.dp, if (highlighted) DeliveryGreen else DeliveryBorder),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = DeliveryGreen, modifier = Modifier.size(24.dp))
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, maxLines = 2, overflow = TextOverflow.Ellipsis, color = DeliveryMuted, fontSize = 10.sp)
        }
    }
}
