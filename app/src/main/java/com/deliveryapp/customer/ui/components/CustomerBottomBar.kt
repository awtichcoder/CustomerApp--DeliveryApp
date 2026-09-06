package com.deliveryapp.customer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.deliveryapp.customer.ui.theme.DeliveryGreen
import com.deliveryapp.customer.ui.theme.DeliveryGreenSoft
import com.deliveryapp.customer.ui.theme.DeliveryMuted
import com.deliveryapp.customer.ui.theme.DeliverySurface

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
        BottomItem("rewards", "Điểm", Icons.Outlined.CardGiftcard, onRewards),
        BottomItem("profile", "Tài khoản", Icons.Outlined.Person, onProfile)
    )
    Surface(
        modifier = Modifier.height(92.dp),
        color = DeliverySurface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        border = BorderStroke(1.dp, DeliveryGreen)
    ) {
        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = selected == item.route,
                    onClick = item.action,
                    icon = { Icon(item.icon, item.label) },
                    label = { Text(item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeliveryGreen,
                        selectedTextColor = DeliveryGreen,
                        unselectedIconColor = DeliveryMuted,
                        unselectedTextColor = DeliveryMuted,
                        indicatorColor = DeliveryGreenSoft
                    )
                )
            }
        }
    }
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector, val action: () -> Unit)
