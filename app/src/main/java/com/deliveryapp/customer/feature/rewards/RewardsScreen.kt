package com.deliveryapp.customer.feature.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deliveryapp.customer.data.dto.LoyaltyAccountResponse
import com.deliveryapp.customer.data.dto.LoyaltyTransactionResponse
import com.deliveryapp.customer.ui.components.AppSurface
import com.deliveryapp.customer.ui.components.DeliveryCard
import com.deliveryapp.customer.ui.components.EmptyState
import com.deliveryapp.customer.ui.components.ErrorState
import com.deliveryapp.customer.ui.components.InfoRow
import com.deliveryapp.customer.ui.components.LoadingState
import com.deliveryapp.customer.ui.components.ScreenHeader
import com.deliveryapp.customer.ui.components.SecondaryAction
import com.deliveryapp.customer.ui.components.SessionExpiredDialog
import com.deliveryapp.customer.ui.components.StatusBanner
import com.deliveryapp.customer.ui.theme.DeliveryBorder
import com.deliveryapp.customer.ui.theme.DeliveryGreen
import com.deliveryapp.customer.ui.theme.DeliveryMuted

@Composable
fun RewardsScreen(
    viewModel: RewardsViewModel,
    onBack: () -> Unit,
    onUsePoints: () -> Unit,
    onSessionExpired: () -> Unit
) {
    val state = viewModel.state
    if (state.sessionExpired) {
        SessionExpiredDialog {
            viewModel.consumeSessionExpired()
            onSessionExpired()
        }
    }
    AppSurface {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                ScreenHeader("Điểm thưởng", "Tích điểm sau mỗi đơn hoàn tất", onBack)
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when {
                        state.isLoading -> LoadingState("Đang tải điểm thưởng")
                        state.errorMessage != null -> ErrorState(state.errorMessage, viewModel::load)
                        state.loyalty != null -> RewardsContent(state.loyalty, onUsePoints)
                        else -> EmptyState("Chưa có dữ liệu", "Không tìm thấy tài khoản điểm thưởng cho khách hàng này.")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RewardsContent(loyalty: LoyaltyAccountResponse, onUsePoints: () -> Unit) {
    StatusBanner(loyalty.tier.uppercase(), "Hạng thành viên • thông tin")
    Text("Số dư và hạng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    DeliveryCard {
        InfoRow("ID người dùng", loyalty.customerId.take(12))
        InfoRow("Điểm hiện có", "${loyalty.points} điểm")
        InfoRow("Hạng", loyalty.tier.uppercase())
        InfoRow("Ưu đãi hạng", "Chưa áp dụng giảm phí")
        InfoRow("Quy đổi", "1 điểm = 1.000đ")
    }
    Text("Quy định đổi điểm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    DeliveryCard {
        Text("1. Giới hạn điểm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Mỗi đơn chỉ có thể dùng tối đa 5 điểm để quy đổi.", style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
        Text("Tương đương mỗi đơn chỉ có thể giảm 5.000đ.", style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
        Text("2. Quy tắc hạng thành viên", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Hạng SILVER/GOLD dùng để hiển thị trong MVP, chưa tự động giảm tổng phí.", style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
        Text("Điểm chỉ được cộng sau khi đơn COMPLETED và mỗi đơn chỉ được cộng một lần.", style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
        Text("Điểm được xem trước và sử dụng trong bước xác nhận tạo đơn.", style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
    }
    if (loyalty.recentTransactions.isNotEmpty()) {
        Text("Giao dịch gần đây", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        DeliveryCard {
            loyalty.recentTransactions.take(5).forEachIndexed { index, transaction ->
                TransactionRow(transaction)
                if (index != loyalty.recentTransactions.take(5).lastIndex) HorizontalDivider(color = DeliveryBorder)
            }
        }
    }
    SecondaryAction("Tạo đơn và dùng điểm", onUsePoints, danger = true)
}

@Composable
private fun TransactionRow(transaction: LoyaltyTransactionResponse) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(transaction.type, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(transaction.orderId?.let { "Đơn ${it.take(8)}" } ?: transaction.createdAt, style = MaterialTheme.typography.bodySmall, color = DeliveryMuted)
        }
        Text(
            if (transaction.pointsDelta >= 0) "+${transaction.pointsDelta}" else transaction.pointsDelta.toString(),
            color = DeliveryGreen,
            fontWeight = FontWeight.Bold
        )
    }
}
