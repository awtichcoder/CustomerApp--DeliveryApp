package com.deliveryapp.customer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
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
        BottomItem("home", "Trang chủ", Icons.Outlined.Home, onHome),
        BottomItem("orders", "Đơn hàng", Icons.AutoMirrored.Outlined.ReceiptLong, onOrders),
        BottomItem("rewards", "Ưu đãi", Icons.Outlined.CardGiftcard, onRewards),
        BottomItem("profile", "Tài khoản", Icons.Outlined.Person, onProfile)
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = selected == item.route,
                onClick = item.action,
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: () -> Unit
)
