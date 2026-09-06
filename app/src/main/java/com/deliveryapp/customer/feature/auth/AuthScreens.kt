package com.deliveryapp.customer.feature.auth

import androidx.compose.runtime.Composable
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.components.ScreenScaffold
import com.deliveryapp.customer.ui.components.SecondaryAction

@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    ScreenScaffold(
        title = "Đăng nhập",
        subtitle = "Khung FE đã sẵn sàng. Nhóm 1 bổ sung form và gọi POST /api/identity/login."
    ) {
        PrimaryAction("Đăng nhập thử", onLogin)
        SecondaryAction("Tạo tài khoản", onRegister)
    }
}

@Composable
fun RegisterScreen(onRegistered: () -> Unit) {
    ScreenScaffold(
        title = "Tạo tài khoản",
        subtitle = "Nhóm 1 triển khai email, mật khẩu, họ tên và số điện thoại."
    ) {
        PrimaryAction("Hoàn tất đăng ký", onRegistered)
    }
}
