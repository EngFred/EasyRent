package com.engineerfred.easyrent.presentation.screens.payments

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerfred.easyrent.presentation.screens.payments.component.PaymentItem
import com.engineerfred.easyrent.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Payments(
    modifier: Modifier = Modifier,
    onAddPayment: () -> Unit,
    onBackClicked: () -> Unit,
    paymentsViewModel: PaymentsViewModel = hiltViewModel()
) {

    val uiState = paymentsViewModel.uiState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(uiState.deletingPaymentErr) {
        if(uiState.deletingPaymentErr != null ) {
            Toast.makeText(context, uiState.deletingPaymentErr, Toast.LENGTH_SHORT).show()
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
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                !uiState.isLoading && !uiState.fetchErr.isNullOrEmpty() -> {
                    Text(text = uiState.fetchErr, style = TextStyle(color = Color.Red, fontWeight = FontWeight.W200, textAlign = TextAlign.Center))
                }

                else -> {
                    when {
                        uiState.payments.isEmpty() -> {
                            Text(text = "There are no payments made!", style = TextStyle( fontWeight = FontWeight.W200, textAlign = TextAlign.Center, fontSize = 20.sp))
                        }
                        else -> {
                            Box(Modifier.fillMaxSize()){
                                Column(
                                    Modifier.fillMaxSize()
                                ) {
                                    if( uiState.payments.isNotEmpty() ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            elevation = CardDefaults.cardElevation(4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.Green.copy(alpha = 0.1f)
                                            )
                                        ) {
                                            val totalAmount = uiState.payments.sumOf { it.amount.toInt() }
                                            Row(
                                                Modifier.fillMaxWidth().padding(20.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(fontWeight = FontWeight.Bold, text = "TOTAL:")
                                                Spacer(Modifier.size(5.dp))
                                                Text(fontWeight = FontWeight.Bold, text = "UGX.${formatCurrency(totalAmount.toFloat())}")
                                            }
                                        }
                                        Spacer(Modifier.size(15.dp))
                                    }
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                Box(Modifier
                                    .fillMaxWidth().padding(vertical = 16.dp, horizontal = 10.dp)
                                    .align(Alignment.BottomCenter), contentAlignment = Alignment.Center){
                                    Text(
                                        "Long press or simply press on any payment to delete!",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.Magenta,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }

    }
}