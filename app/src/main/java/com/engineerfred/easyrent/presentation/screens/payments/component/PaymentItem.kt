package com.engineerfred.easyrent.presentation.screens.payments.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.modals.PaymentMethod
import com.engineerfred.easyrent.presentation.common.CustomAlertDialog
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.util.formatCurrency
import com.engineerfred.easyrent.util.toFormattedDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentItem(
    payment: Payment,
    deletingPayment: Boolean,
    deletedPaymentId: String,
    onConfirmDeletePayment: (Payment) -> Unit
) {
    val borderColor = if (payment.isSynced) Color(0xFF4CAF50) else Color(0xFFF44336) // Green for synced, Red for not synced
    var showConfirmDeletePaymentDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .shadow(6.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = {
                    if (!deletingPayment) {
                        showConfirmDeletePaymentDialog = true
                    }
                }
            )
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MyCardBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Room ${payment.roomNumber} - ${payment.tenantName.replaceFirstChar { it.uppercase() }}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MySurface
                )

                Text(
                    text = "Paid: UGX ${formatCurrency(payment.amount)}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = MyPrimary,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = .5f),
                            blurRadius = 6f,
                            offset = Offset(2f, 2f)
                        )
                    )
                )

                Text(
                    text = "Balance: UGX ${formatCurrency(payment.newBalance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MySurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = payment.paymentDate.toFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MySurface
                    )

                    Spacer(Modifier.size(6.dp))

                    val pm = when (payment.paymentMethod) {
                        PaymentMethod.MobileMoney.name -> "Mobile Money"
                        PaymentMethod.CreditCard.name -> "Credit Card"
                        else -> payment.paymentMethod
                    }

                    Text(
                        text = pm,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MySurface,
                    )
                }
            }
        }
        if (deletingPayment && deletedPaymentId == payment.id) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (showConfirmDeletePaymentDialog) {
            CustomAlertDialog(
                title = "Delete Payment",
                text1 = "Do you want to delete this payment by ",
                boldText1 = "${payment.tenantName}? ",
                text2 = "This action cannot be undone.",
                confirmButtonText = "Yes, Delete",
                onConfirm = {
                    showConfirmDeletePaymentDialog = false
                    onConfirmDeletePayment(payment)
                },
                onDismiss = { showConfirmDeletePaymentDialog = false }
            )
        }
    }
}

