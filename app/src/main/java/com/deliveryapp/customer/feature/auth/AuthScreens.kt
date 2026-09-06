package com.deliveryapp.customer.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.deliveryapp.customer.ui.components.AppSurface
import com.deliveryapp.customer.ui.components.AppTextField
import com.deliveryapp.customer.ui.components.GreenHeader
import com.deliveryapp.customer.ui.components.PrimaryAction
import com.deliveryapp.customer.ui.theme.DeliveryGreen
import com.deliveryapp.customer.ui.theme.DeliveryMuted

@Composable
fun LoginScreen(viewModel: AuthViewModel, onLogin: () -> Unit, onRegister: () -> Unit) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.loginCompleted) {
        if (state.loginCompleted) {
            viewModel.consumeLoginCompleted()
            onLogin()
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    AuthScaffold(snackbarHostState) {
        GreenHeader("DeliveryApp", "Giao nhanh, an tâm từng chặng")
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Chào mừng trở lại", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Đăng nhập để quản lý và theo dõi đơn hàng",
                style = MaterialTheme.typography.bodyMedium,
                color = DeliveryMuted
            )
            AppTextField(
                value = state.loginEmail,
                onValueChange = viewModel::onLoginEmailChange,
                label = "Email",
                error = state.loginEmailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            AppTextField(
                value = state.loginPassword,
                onValueChange = viewModel::onLoginPasswordChange,
                label = "Mật khẩu",
                error = state.loginPasswordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            PrimaryAction("Đăng nhập", viewModel::submitLogin, loading = state.isLoading)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản?", color = DeliveryMuted, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onRegister) {
                    Text("Đăng ký ngay", color = DeliveryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onRegistered: () -> Unit, onBack: () -> Unit) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.registerCompleted) {
        if (state.registerCompleted) {
            viewModel.consumeRegisterCompleted()
            onRegistered()
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    AuthScaffold(snackbarHostState) {
        GreenHeader("DeliveryApp", "Giao nhanh, an tâm từng chặng")
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tạo tài khoản", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Điền đúng 4 trường theo API đăng ký", style = MaterialTheme.typography.bodyMedium, color = DeliveryMuted)
            AppTextField(
                state.registerName,
                viewModel::onRegisterNameChange,
                "Họ và tên • bắt buộc",
                error = state.registerNameError
            )
            AppTextField(
                state.registerEmail,
                viewModel::onRegisterEmailChange,
                "Email",
                error = state.registerEmailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            AppTextField(
                state.registerPassword,
                viewModel::onRegisterPasswordChange,
                "Mật khẩu",
                error = state.registerPasswordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            AppTextField(
                state.registerPhone,
                viewModel::onRegisterPhoneChange,
                "Số điện thoại • bắt buộc",
                error = state.registerPhoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            PrimaryAction("Đăng ký", viewModel::submitRegister, loading = state.isLoading)
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Quay lại đăng nhập") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AuthScaffold(snackbarHostState: SnackbarHostState, content: @Composable () -> Unit) {
    AppSurface {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
                content = { content() }
            )
        }
    }
}

@Composable
fun SessionCheckScreen() {
    AppSurface {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            GreenHeader("DeliveryApp", "Đang kiểm tra phiên đăng nhập")
        }
    }
}
