package com.engineerfred.easyrent.presentation.screens.payments

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkManager
import com.engineerfred.easyrent.presentation.common.CustomSyncToast
import com.engineerfred.easyrent.presentation.screens.payments.component.PaymentItem
import com.engineerfred.easyrent.presentation.theme.LightSkyBlue
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary
import com.engineerfred.easyrent.util.WorkerUtils
import com.engineerfred.easyrent.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Payments(
    onAddPayment: () -> Unit,
    onBackClicked: () -> Unit,
    paymentsViewModel: PaymentsViewModel = hiltViewModel(),
    workManager: WorkManager
) {

    val uiState = paymentsViewModel.uiState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(uiState.deletingPaymentErr) {
        if(uiState.deletingPaymentErr != null ) {
            Toast.makeText(context, uiState.deletingPaymentErr, Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            paymentsViewModel.hideSyncButton()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Payments") },
                navigationIcon = {
                    IconButton(onClick = { onBackClicked() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MySecondary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            onAddPayment()
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.AddCircle, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible= uiState.showSyncButton) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 45.dp, end = 20.dp),
                    onClick = {
                        WorkerUtils.syncPaymentsImmediately(workManager)
                        paymentsViewModel.hideSyncButton()
                    },
                    containerColor = MyPrimary
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                }

                !uiState.isLoading && !uiState.fetchErr.isNullOrEmpty() -> {
                    Text(
                        text = uiState.fetchErr,
                        style = TextStyle(
                            fontWeight = FontWeight.W200,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            color = MyError,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = .5f),
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
                    )
                }

                else -> {
                    when {
                        uiState.payments.isEmpty() -> {
                            Text(style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.W200,
                                textAlign = TextAlign.Center,
                                color = MySurface,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = .5f),
                                    blurRadius = 6f,
                                    offset = Offset(2f, 2f)
                                )
                            ), text = "There are no payments made!")
                        }
                        else -> {
                            Box(Modifier.fillMaxWidth().weight(1f)){
                                Column(
                                    Modifier.fillMaxSize()
                                ) {
                                    if( uiState.payments.isNotEmpty() ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 15.dp),
                                            elevation = CardDefaults.cardElevation(4.dp)
                                        ) {
                                            val totalAmount = uiState.payments.sumOf { it.amount.toInt() }
                                            Row(
                                                Modifier.fillMaxWidth()
                                                    .background(Brush.verticalGradient(listOf(MyTertiary, LightSkyBlue)))
                                                    .padding(20.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(fontWeight = FontWeight.Bold, text = "TOTAL:", color = MySurface)
                                                Spacer(Modifier.size(5.dp))
                                                Text(fontWeight = FontWeight.Bold, text = "UGX.${formatCurrency(totalAmount.toFloat())}", color = MySurface)
                                            }
                                        }
                                    }
                                    LazyVerticalStaggeredGrid(
                                        columns = StaggeredGridCells.Fixed(2),
                                        verticalItemSpacing = 16.dp,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        items(count = uiState.payments.size, key = { uiState.payments[it].id }) { paymentIndex ->
                                            PaymentItem(
                                                payment = uiState.payments[paymentIndex],
                                                deletingPayment = uiState.deletingPayment,
                                                deletedPaymentId = uiState.deletedPaymentId ?: "",
                                                onConfirmDeletePayment = {
                                                    paymentsViewModel.onEvent(PaymentsUiEvents.PaymentDeleted(it))
                                                }
                                            )
                                        }
                                    }
                                }
                                CustomSyncToast(
                                    showSyncRequired = uiState.showSyncRequired,
                                    dataCount = uiState.unSyncedPayments.size,
                                    dataName = "payment"
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { paymentsViewModel.onEvent(PaymentsUiEvents.PaymentFilterToggled) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (uiState.showCurrentMonthPaymentsOnly) MyPrimary else Color.LightGray,
                                            contentColor = if (uiState.showCurrentMonthPaymentsOnly) Color.White else Color.Black

                                        )
                                    ) {
                                        Text(text = "This Month")
                                    }

                                    Button(
                                        onClick = { paymentsViewModel.onEvent(PaymentsUiEvents.PaymentFilterToggled) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!uiState.showCurrentMonthPaymentsOnly) MyPrimary else Color.LightGray,
                                            contentColor = if (!uiState.showCurrentMonthPaymentsOnly) Color.White else Color.Black
                                        )
                                    ) {
                                        Text(text = "All Payments")
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "To delete payment, simply long press on it!",
                                    modifier = Modifier.fillMaxWidth(),
                                    style = TextStyle(
                                        color = Color.Cyan,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 15.sp,
                                        shadow = Shadow(
                                            color = Color.Black,
                                            blurRadius = 6f,
                                            offset = Offset(2f, 2f)
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}