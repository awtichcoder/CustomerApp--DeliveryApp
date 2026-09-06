package com.deliveryapp.customer.feature.rewards

import androidx.compose.runtime.Composable
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.components.ScreenScaffold
import com.deliveryapp.customer.ui.components.SecondaryAction

@Composable
fun RewardsScreen(onBack: () -> Unit, onUsePoints: () -> Unit) {
    ScreenScaffold(
        title = "Điểm thưởng",
        subtitle = "Nhóm 1 hiển thị số dư, hạng thành viên và lịch sử điểm."
    ) {
        PrimaryAction("Tạo đơn và dùng điểm", onUsePoints)
        SecondaryAction("Về trang chủ", onBack)
    }
}
