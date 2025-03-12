package com.engineerfred.easyrent.presentation.screens.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerfred.easyrent.domain.modals.Expense
import com.engineerfred.easyrent.domain.modals.ExpenseCategory
import com.engineerfred.easyrent.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect( uiState.insertSuccess ) {
        if( uiState.insertSuccess ) {
            showDialog = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onBack
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = null)
                            }
                            Text(
                                text = "Expenses",
                                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).padding(end = 70.dp)
                            )
                        }
                        Spacer(Modifier.size(20.dp))
                        HorizontalDivider(thickness = 6.dp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when {
                uiState.isFetching -> {
                    LinearProgressIndicator(modifier = Modifier.width(100.dp))
                }

                uiState.isFetching.not() && uiState.fetchErr != null -> {
                    ErrorMessage(
                        errorMessage = uiState.fetchErr,
                        onRetry = {
                            viewModel.onEvent(ExpensesUiEvents.OnRetry)
                        }
                    )
                }

                else -> {
                    when {
                        uiState.expenses.isEmpty() -> {
                            Text(
                                text = "There are no expenses yet. Click the button below to add!",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        else -> {
                            val expenses = uiState.expenses
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(count = expenses.size, key = { expenses[it].id }) { index ->
                                        val expense = expenses[index]
                                        ExpenseItem(
                                            expense,
                                            onDelete = { viewModel.onEvent(ExpensesUiEvents.DeleteButtonClicked(expense)) }
                                        )
                                    }
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Green.copy(alpha = 0.1f)
                                    )
                                ) {
                                    val totalAmount = uiState.expenses.sumOf { it.amount.toInt() }
                                    Row(
                                        Modifier.fillMaxWidth().padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fontWeight = FontWeight.Bold, text = "TOTAL:")
                                        Spacer(Modifier.size(5.dp))
                                        Text(fontWeight = FontWeight.Bold, text = "UGX.${formatCurrency(totalAmount.toFloat())}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddExpenseDialog(
            onDismiss = { showDialog = false },
            viewModel = viewModel,
            uiState = uiState
        )
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.title.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "UGX.${formatCurrency(expense.amount)}", color = Color.Gray, fontSize = 16.sp)
                Text(text = expense.category, color = Color.Blue, fontSize = 14.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sync Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    val syncColor = if (expense.isSynced) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    val syncText = if (expense.isSynced) "Synced" else "Not Synced"
                    val syncIcon = if (expense.isSynced) Icons.Filled.CloudDone else Icons.Filled.CloudOff

                    Icon(imageVector = syncIcon, contentDescription = "Sync Status", tint = syncColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = syncText, color = syncColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                // Delete Button
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    uiState: ExpensesUiState,
    viewModel: ExpensesViewModel
) {

    var expandCategoriesMenu by remember {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = {
                        viewModel.onEvent(ExpensesUiEvents.TitleChanged(it))
                    },
                    singleLine = true,
                    label = { Text("Title") },
                    isError = uiState.title.isEmpty() && uiState.title.length < 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = {
                        viewModel.onEvent(ExpensesUiEvents.AmountChanged(it))
                    },
                    singleLine = true,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.amount.isEmpty() || uiState.amount.toFloatOrNull() == null
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandCategoriesMenu,
                    onExpandedChange = { expandCategoriesMenu = it }
                ) {
                    OutlinedTextField(
                        value = uiState.category ?: "Choose category",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandCategoriesMenu)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expandCategoriesMenu,
                        onDismissRequest = { expandCategoriesMenu = false }
                    ) {
                        ExpenseCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    viewModel.onEvent(ExpensesUiEvents.ChangedCategory(cat.displayName))
                                    expandCategoriesMenu = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.notes ?: "",
                    onValueChange = {
                        viewModel.onEvent(ExpensesUiEvents.NotesChanged(it))
                    },
                    label = { Text("Notes") },
                    singleLine = true,
                    isError = uiState.notes.isNullOrEmpty().not() && uiState.notes?.length!! < 10
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (uiState.isValidForm && uiState.isInserting.not()) {
                    viewModel.onEvent(ExpensesUiEvents.SaveButtonClicked)
                    onDismiss()
                }
            }, enabled = uiState.isInserting.not() && uiState.isValidForm) {
                if( uiState.isInserting ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            Button(onClick = { if ( uiState.isInserting.not() ) onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ErrorMessage(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Retry")
            }
        }
    }
}


