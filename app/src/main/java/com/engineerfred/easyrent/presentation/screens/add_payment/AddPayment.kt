package com.engineerfred.easyrent.presentation.screens.add_payment

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerfred.easyrent.domain.modals.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPayment(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    addPaymentViewModel: AddPaymentViewModel = hiltViewModel()
) {
    val uiState by addPaymentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.insertionSuccess) {
        if (uiState.insertionSuccess) {
            onBack()
        }
    }

    LaunchedEffect(key1 = uiState.insertionErr) {
        if ( uiState.insertionErr != null ) {
            Toast.makeText(context, uiState.insertionErr, Toast.LENGTH_LONG).show()
        }
    }

    var expandedTenantDropdown by remember { mutableStateOf(false) }
    var expandedPaymentMethodDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Amount Input with Icon
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                OutlinedTextField(
                    value = uiState.amount ?: "",
                    onValueChange = {
                        if ( uiState.selectedTenant != null ) {
                            addPaymentViewModel.onEvent(AddPaymentUiEvents.AmountChanged(it))
                        } else {
                            Toast.makeText(context, "Select a tenant first!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    label = { Text("Enter Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "Amount")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tenant Selection
            ExposedDropdownMenuBox(
                expanded = expandedTenantDropdown,
                onExpandedChange = { expandedTenantDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedTenant?.name ?: "Select Tenant",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTenantDropdown)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expandedTenantDropdown,
                    onDismissRequest = { expandedTenantDropdown = false }
                ) {
                    uiState.tenants.forEach { tenant ->
                        DropdownMenuItem(
                            text = { Text(tenant.name) },
                            onClick = {
                                addPaymentViewModel.onEvent(AddPaymentUiEvents.SelectedTenant(tenant))
                                expandedTenantDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Selection
            ExposedDropdownMenuBox(
                expanded = expandedPaymentMethodDropdown,
                onExpandedChange = { expandedPaymentMethodDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaymentMethodDropdown)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expandedPaymentMethodDropdown,
                    onDismissRequest = { expandedPaymentMethodDropdown = false }
                ) {
                    PaymentMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.displayName) },
                            onClick = {
                                addPaymentViewModel.onEvent(AddPaymentUiEvents.PaymentMethodChanged(method.displayName))
                                expandedPaymentMethodDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = { addPaymentViewModel.onEvent(AddPaymentUiEvents.SaveClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState.selectedTenant != null &&
                        uiState.room != null &&
                        !uiState.amount.isNullOrEmpty() &&
                        uiState.amount?.toFloatOrNull() != null &&
                        uiState.balanceCalcErr == null &&
                        uiState.balance != null &&
                        !uiState.isInserting
            ) {
                if (uiState.isInserting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Submit Payment", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rent & Balance Information
            AnimatedVisibility(visible = uiState.room != null && uiState.selectedTenant != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val b = if( uiState.room!!.monthlyRent == uiState.selectedTenant!!.balance ) "Total Rent: ${uiState.room?.monthlyRent}" else "Balance: ${uiState.selectedTenant?.balance}"
                        Text(
                            text = b,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = uiState.room != null && uiState.amount?.toFloatOrNull() != null && uiState.balance != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.balanceCalcErr != null) Color.Red.copy(alpha = 0.1f) else Color.Green.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.balanceCalcErr != null) {
                            Text(
                                text = "The amount you are trying to pay exceeds the room's monthly rent!",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Calculated Balance: ${uiState.balance}",
                                color = Color.Green,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if ( uiState.selectedTenant == null ) {
                Box(Modifier
                    .fillMaxWidth()
                    .weight(1f), contentAlignment = Alignment.BottomCenter){
                    Text("Select a tenant first, to proceed with making a payment!", fontSize = 15.sp, textAlign = TextAlign.Center, color = Color.Magenta)
                }
            }
        }
    }
}
