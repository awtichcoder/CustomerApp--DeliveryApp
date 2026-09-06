package com.deliveryapp.customer.feature.order

import androidx.compose.runtime.Composable
import com.deliveryapp.customer.ui.components.CustomerBottomBar
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.components.ScreenScaffold
import com.deliveryapp.customer.ui.components.SecondaryAction

@Composable
fun CreateOrderScreen(onBack: () -> Unit, onCreated: () -> Unit) {
    ScreenScaffold(
        title = "Tạo đơn giao hàng",
        subtitle = "Nhóm 2 bổ sung địa chỉ, tọa độ, số điện thoại, mã ưu đãi và điểm."
    ) {
        PrimaryAction("Tạo đơn thử", onCreated)
        SecondaryAction("Quay lại", onBack)
    }
}

@Composable
fun OrderDetailScreen(onBack: () -> Unit, onTrack: () -> Unit, onCancelled: () -> Unit) {
    ScreenScaffold(
        title = "Chi tiết đơn hàng",
        subtitle = "Trạng thái SEARCHING. Sau khi có tài xế, mở màn hình theo dõi."
    ) {
        PrimaryAction("Theo dõi tài xế", onTrack)
        SecondaryAction("Hủy đơn", onCancelled)
        SecondaryAction("Quay lại", onBack)
    }
}

@Composable
fun OrdersScreen(
    onTrack: () -> Unit,
    onHome: () -> Unit,
    onRewards: () -> Unit,
    onProfile: () -> Unit
) {
    ScreenScaffold(
        title = "Đơn hàng của bạn",
        subtitle = "Nhóm 2 triển khai danh sách, lọc trạng thái và phân trang.",
        bottomBar = {
            CustomerBottomBar("orders", onHome, {}, onRewards, onProfile)
        }
    ) {
        PrimaryAction("Theo dõi đơn mẫu", onTrack)
    }
}
