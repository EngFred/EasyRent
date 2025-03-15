package com.engineerfred.easyrent.presentation.screens.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudSync
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkManager
import com.engineerfred.easyrent.domain.modals.Expense
import com.engineerfred.easyrent.domain.modals.ExpenseCategory
import com.engineerfred.easyrent.presentation.common.CustomAlertDialog
import com.engineerfred.easyrent.presentation.common.CustomSyncToast
import com.engineerfred.easyrent.presentation.common.CustomTextField
import com.engineerfred.easyrent.presentation.theme.LightSkyBlue
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary
import com.engineerfred.easyrent.util.WorkerUtils
import com.engineerfred.easyrent.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    workManager: WorkManager
) {
    val uiState = viewModel.uiState.collectAsState().value

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    LaunchedEffect( uiState.insertSuccess ) {
        if( uiState.insertSuccess ) {
            showAddExpenseDialog = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.hideSyncButton()
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                modifier = Modifier.padding(bottom = 36.dp, end = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = uiState.showSyncButton ) {
                    IconButton(
                        onClick = {
                            WorkerUtils.syncExpensesImmediately(workManager)
                            viewModel.hideSyncButton()
                        },
                        modifier = Modifier.size(60.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MyPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudSync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(Modifier.size(10.dp))
                IconButton(
                    onClick = {
                        showAddExpenseDialog = true
                    },
                    modifier = Modifier.size(60.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MyPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        topBar = {
            TopAppBar(title = { Text(text = "Expenses") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
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
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
                .padding(paddingValues).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when {
                uiState.isFetching -> {
                    LinearProgressIndicator(modifier = Modifier.width(100.dp), color = Color.White)
                }

                uiState.fetchErr != null -> {
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
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MySurface,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = .5f),
                                        blurRadius = 6f,
                                        offset = Offset(2f, 2f)
                                    )
                                )
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
                                            onDelete = {
                                                viewModel.onEvent(ExpensesUiEvents.DeletedExpense(expense))
                                            }
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
                                CustomSyncToast(
                                    showSyncRequired = uiState.showSyncRequired,
                                    dataCount = uiState.unSyncedExpenses.size,
                                    dataName = "expense"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            viewModel = viewModel,
            uiState = uiState
        )
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onDelete: () -> Unit
) {

    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .background(MyCardBg)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.title.replaceFirstChar { it.uppercase() },
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MySurface,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = .5f),
                            blurRadius = 6f,
                            offset = Offset(2f, 2f)
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "UGX.${formatCurrency(expense.amount)}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = MySurface,
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 6f,
                            offset = Offset(2f, 2f)
                        )
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = expense.category,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = MyPrimary,
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 6f,
                            offset = Offset(2f, 2f)
                        )
                    )
                )
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
                    Text(
                        text = syncText,
                        style = TextStyle(
                            color = syncColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
                    )
                }

                // Delete Button
                IconButton(onClick = {
                    showConfirmDeleteDialog = true
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = .5f))
                }
            }
        }
    }

    if ( showConfirmDeleteDialog ) {
        CustomAlertDialog(
            onDismiss = { showConfirmDeleteDialog = false },
            confirmButtonText = "Delete",
            cancelButtonText = "Cancel",
            title = "Delete Expense",
            text1 = "Are you sure you want to delete expense: ",
            boldText1 = expense.title.replaceFirstChar { it.uppercase() },
            text2 = " with amount: ",
            boldText2 = "UGX.${formatCurrency(expense.amount)}",
            text3 = "? This action cannot be undone!" ,
            onConfirm = {
                showConfirmDeleteDialog = false
                onDelete()
            }
        )
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
        containerColor =  MyTertiary,
        title = {
            Text(
                "Add Expense",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 24.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = .5f),
                        blurRadius = 6f,
                        offset = Offset(2f, 2f)
                    )
                )
            )
        },
        text = {
            Column {

                CustomTextField(
                    value = uiState.title,
                    onValueChange = {
                        viewModel.onEvent(ExpensesUiEvents.TitleChanged(it))
                    },
                    label = "Title",
                    errorMessage = uiState.titleErr
                )

                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = uiState.amount,
                    onValueChange = {
                        viewModel.onEvent(ExpensesUiEvents.AmountChanged(it))
                    },
                    label = "Amount",
                    keyboardType = KeyboardType.Number,
                    errorMessage = uiState.amountErr
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
                        expanded = expandCategoriesMenu,
                        onDismissRequest = { expandCategoriesMenu = false },
                        modifier = Modifier.background(Brush.horizontalGradient(listOf(MyTertiary, LightSkyBlue)))
                    ) {
                        ExpenseCategory.entries.forEachIndexed { i, cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${i + 1}. ${cat.displayName}",
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
                                    )
                                },
                                onClick = {
                                    viewModel.onEvent(ExpensesUiEvents.ChangedCategory(cat.displayName))
                                    expandCategoriesMenu = false
                                }
                            )
                            if ( i != ExpenseCategory.entries.lastIndex ) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = uiState.notes ?: "",
                    onValueChange = {
                        viewModel.onEvent(ExpensesUiEvents.NotesChanged(it))
                    },
                    label = "Notes (Optional)"
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.onEvent(ExpensesUiEvents.SaveButtonClicked)
                onDismiss()
            }, enabled = uiState.isInserting.not() && uiState.titleErr == null && uiState.amountErr == null && uiState.category != null && uiState.title.isNotEmpty() && uiState.amount.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyPrimary,
                    disabledContainerColor = MyPrimary
                )
            ) {
                if( uiState.isInserting ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = { if ( uiState.isInserting.not() ) onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    disabledContainerColor = Color.LightGray,
                    contentColor = Color.Black,
                    disabledContentColor = Color.Black
                )
            ) {
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(MyError, MyTertiary)))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = "Error",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MySurface,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = .5f),
                        blurRadius = 6f,
                        offset = Offset(2f, 2f)
                    )
                )
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MyPrimary, contentColor = Color.White)
            ) {
                Text("Retry")
            }
        }
    }
}


