package com.deliveryapp.customer.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CustomerBottomBar(
    selected: String,
    onHome: () -> Unit,
    onOrders: () -> Unit,
    onRewards: () -> Unit,
    onProfile: () -> Unit
) {
    val items = listOf(
        Triple("home", "Trang chủ", onHome),
        Triple("orders", "Đơn hàng", onOrders),
        Triple("rewards", "Điểm", onRewards),
        Triple("profile", "Tài khoản", onProfile)
    )
    NavigationBar {
        items.forEach { (route, label, action) ->
            NavigationBarItem(
                selected = selected == route,
                onClick = action,
                icon = { Text(label.take(1)) },
                label = { Text(label) }
            )
        }
    }
}
