package com.deliveryapp.customer.feature.tracking

import androidx.compose.runtime.Composable
import com.deliveryapp.customer.ui.components.ScreenScaffold
import com.deliveryapp.customer.ui.components.SecondaryAction

@Composable
fun TrackingScreen(onBack: () -> Unit) {
    ScreenScaffold(
        title = "Theo dõi tài xế",
        subtitle = "Nhóm 2 hiển thị bản đồ và cập nhật GET /api/delivery/{deliveryId}/location."
    ) {
        SecondaryAction("Về danh sách đơn", onBack)
    }
}
