package com.deliveryapp.customer.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deliveryapp.customer.ui.components.AppSurface
import com.deliveryapp.customer.ui.components.CustomerBottomBar
import com.deliveryapp.customer.ui.components.DeliveryCard
import com.deliveryapp.customer.ui.components.ErrorState
import com.deliveryapp.customer.ui.components.InfoRow
import com.deliveryapp.customer.ui.components.LoadingState
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.components.ScreenHeader
import com.deliveryapp.customer.ui.components.SecondaryAction
import com.deliveryapp.customer.ui.components.SessionExpiredDialog
import com.deliveryapp.customer.ui.components.StatusBanner
import com.deliveryapp.customer.ui.theme.DeliveryGreen
import com.deliveryapp.customer.ui.theme.DeliveryMuted

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onHome: () -> Unit,
    onOrders: () -> Unit,
    onRewards: () -> Unit,
    onSessionExpired: () -> Unit
) {
    val state = viewModel.state
    LaunchedEffect(state.logoutCompleted) {
        if (state.logoutCompleted) {
            viewModel.consumeLogoutCompleted()
            onLogout()
        }
    }
    if (state.sessionExpired) {
        SessionExpiredDialog {
            viewModel.consumeSessionExpired()
            onSessionExpired()
        }
    }

    AppSurface {
        Scaffold(bottomBar = { CustomerBottomBar("profile", onHome, onOrders, onRewards, {}) }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                ScreenHeader(
                    title = "Tài khoản",
                    subtitle = "Thành viên hạng: ${state.tier.uppercase()}",
                    action = {
                        IconButton(onClick = viewModel::load) {
                            Icon(Icons.Outlined.NotificationsNone, "Làm mới", tint = DeliveryGreen)
                        }
                    }
                )
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when {
                        state.isLoading -> LoadingState("Đang tải tài khoản")
                        state.errorMessage != null && state.user == null -> ErrorState(state.errorMessage, viewModel::load)
                        else -> AccountContent(state, viewModel::load, viewModel::logout)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountContent(state: ProfileUiState, onRefresh: () -> Unit, onLogoutClick: () -> Unit) {
    val user = state.user
    StatusBanner(
        title = user?.role?.uppercase() ?: "CUSTOMER",
        subtitle = "Vai trò từ tài khoản đăng nhập"
    )
    Text("Thông tin cá nhân", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    DeliveryCard {
        InfoRow("ID người dùng", user?.userId?.take(12) ?: "-")
        InfoRow("Email", user?.email ?: "-")
        InfoRow("Họ và tên", user?.fullName ?: "-")
        InfoRow("Điện thoại", user?.phone ?: "-")
    }
    PrimaryAction("Cập nhật thông tin", onRefresh)
    Text("Phiên đăng nhập", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    DeliveryCard {
        Text("Phiên đăng nhập đang hoạt động", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Ứng dụng tự làm mới token và yêu cầu đăng nhập lại khi hết phiên.",
            style = MaterialTheme.typography.bodySmall,
            color = DeliveryGreen
        )
    }
    SecondaryAction(
        label = if (state.isLoggingOut) "Đang đăng xuất…" else "Đăng xuất",
        onClick = onLogoutClick,
        enabled = !state.isLoggingOut,
        danger = true
    )
    state.errorMessage?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
    }
}
