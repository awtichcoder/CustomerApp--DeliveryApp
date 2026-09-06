package com.deliveryapp.customer.feature.home

import androidx.compose.runtime.Composable
import com.deliveryapp.customer.ui.components.CustomerBottomBar
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.components.ScreenScaffold
import com.deliveryapp.customer.ui.components.SecondaryAction

@Composable
fun HomeScreen(
    onCreateOrder: () -> Unit,
    onOrders: () -> Unit,
    onRewards: () -> Unit,
    onProfile: () -> Unit
) {
    ScreenScaffold(
        title = "DeliveryApp",
        subtitle = "Trang chủ Customer theo luồng Figma v3.",
        bottomBar = {
            CustomerBottomBar("home", {}, onOrders, onRewards, onProfile)
        }
    ) {
        PrimaryAction("Tạo đơn giao hàng", onCreateOrder)
        SecondaryAction("Xem đơn gần đây", onOrders)
        SecondaryAction("Xem điểm thưởng", onRewards)
    }
}
