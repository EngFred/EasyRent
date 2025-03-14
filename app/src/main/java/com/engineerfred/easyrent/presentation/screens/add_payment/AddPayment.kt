package com.engineerfred.easyrent.presentation.screens.add_payment

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerfred.easyrent.domain.modals.PaymentMethod
import com.engineerfred.easyrent.presentation.theme.LightSkyBlue
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPayment(
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MySecondary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
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
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Amount Input with Icon
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
                    Text(
                        text = "UGX.",
                        style = TextStyle(
                            color = MySurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(start = 15.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MySurface,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = MySurface,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = MySurface,
                    focusedTextColor = MySurface,
                    unfocusedTextColor = MySurface,
                    focusedLeadingIconColor = MySurface,
                    unfocusedLeadingIconColor = MySurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tenant Selection
            ExposedDropdownMenuBox(
                expanded = expandedTenantDropdown,
                onExpandedChange = { expandedTenantDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedTenant?.name?.replaceFirstChar { it.uppercase() } ?: "Select Tenant",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTenantDropdown)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MySurface,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = MySurface,
                        unfocusedLabelColor = Color.LightGray,
                        cursorColor = MySurface,
                        focusedTextColor = MySurface,
                        unfocusedTextColor = MySurface,
                        focusedTrailingIconColor = MySurface,
                        unfocusedTrailingIconColor = MySurface
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedTenantDropdown,
                    onDismissRequest = { expandedTenantDropdown = false },
                    modifier = Modifier.background(Brush.horizontalGradient(listOf(MyTertiary, LightSkyBlue)))
                ) {
                    uiState.tenants.forEachIndexed { index, tenant ->
                        DropdownMenuItem(
                            text = {
                                val tenantNo = "${index + 1}."
                                Text(
                                text = "$tenantNo ${tenant.name.replaceFirstChar { it.uppercase() }} - Room ${tenant.roomNumber}",
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp,
                                    color = MySurface,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = .5f),
                                        blurRadius = 6f,
                                        offset = Offset(2f, 2f)
                                    )
                                )
                            ) } ,
                            onClick = {
                                addPaymentViewModel.onEvent(AddPaymentUiEvents.SelectedTenant(tenant))
                                expandedTenantDropdown = false
                            }
                        )
                        if ( tenant != uiState.tenants.last() ) {
                            HorizontalDivider()
                        }
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
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MySurface,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = MySurface,
                        unfocusedLabelColor = Color.LightGray,
                        cursorColor = MySurface,
                        focusedTextColor = MySurface,
                        unfocusedTextColor = MySurface,
                        focusedTrailingIconColor = MySurface,
                        unfocusedTrailingIconColor = MySurface
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedPaymentMethodDropdown,
                    onDismissRequest = { expandedPaymentMethodDropdown = false },
                    modifier = Modifier.background(Brush.horizontalGradient(listOf(MyTertiary,LightSkyBlue)))
                ) {
                    PaymentMethod.entries.forEachIndexed { index, method ->
                        DropdownMenuItem(
                            text = { Text(
                                text = "${index + 1}. ${method.displayName}",
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp,
                                    color = MySurface,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = .5f),
                                        blurRadius = 6f,
                                        offset = Offset(2f, 2f)
                                    )
                                )
                            ) },
                            onClick = {
                                addPaymentViewModel.onEvent(AddPaymentUiEvents.PaymentMethodChanged(method.displayName))
                                expandedPaymentMethodDropdown = false
                            }
                        )
                        if ( method != PaymentMethod.entries.last() ) {
                            HorizontalDivider()
                        }
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
                        !uiState.isInserting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyPrimary,
                    disabledContainerColor = MyPrimary
                )
            ) {
                if (uiState.isInserting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
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
                    Column(modifier = Modifier.fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MyTertiary,
                                        LightSkyBlue
                                    )
                                )
                            )
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val b = if( uiState.room!!.monthlyRent == uiState.selectedTenant!!.balance ) "Rent: UGX.${uiState.room?.monthlyRent}" else "Balance: UGX.${uiState.selectedTenant?.balance}"
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
                ) {
                    val bg = if (uiState.balanceCalcErr != null) MyError else  MyCardBg
                    Column(
                        modifier = Modifier.fillMaxWidth().background(bg).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (uiState.balanceCalcErr != null) {
                            Text(
                                text = "Invalid Entry. ${uiState.balanceCalcErr}",
                                color = MySurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "New Balance: UGX.${uiState.balance}",
                                style = TextStyle(
                                    color = MySurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
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
