package com.deliveryapp.customer.feature.profile

import androidx.compose.runtime.Composable
import com.deliveryapp.customer.ui.components.CustomerBottomBar
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.components.ScreenScaffold

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onHome: () -> Unit,
    onOrders: () -> Unit,
    onRewards: () -> Unit
) {
    ScreenScaffold(
        title = "Tài khoản",
        subtitle = "Nhóm 1 lấy hồ sơ từ GET /api/identity/me và xử lý đăng xuất.",
        bottomBar = {
            CustomerBottomBar("profile", onHome, onOrders, onRewards, {})
        }
    ) {
        PrimaryAction("Đăng xuất", onLogout)
    }
}
