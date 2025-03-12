package com.engineerfred.easyrent.presentation.screens.add_room

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomScreen(
    roomId: String?,
    modifier: Modifier = Modifier,
    onAddTenant: (roomId: String, monthlyRent: String, roomNumber: String) -> Unit,
    onSaveCompleted: () -> Unit,
    onCancel: () -> Unit,
    addRoomViewModel: AddRoomViewModel = hiltViewModel()
) {

    val uiState = addRoomViewModel.uiState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.createdRoomId, key2 = uiState.isOccupied) {
        if (uiState.createdRoomId != null && uiState.isOccupied) {
            Log.i("U_I", "Created room id: ${uiState.createdRoomId}")
            onAddTenant(uiState.createdRoomId, uiState.monthlyPayment!!, uiState.roomNumber)
        }

        if( uiState.createdRoomId != null && !uiState.isOccupied ) {
            onSaveCompleted()
        }
    }

    LaunchedEffect(key1 = uiState.insertionErr) {
        if ( uiState.insertionErr != null ) {
            Toast.makeText(context, uiState.insertionErr, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (roomId == null) "Create Room" else "Update Room", style = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = {
                        onCancel()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Room Type Dropdown
            Text(text = "Room Type", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            var expanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(text = uiState.selectedRoomType.name)
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    RoomType.entries.forEach { type ->
                        DropdownMenuItem(
                            onClick = {
                                addRoomViewModel.onEvent(AddRoomUiEvents.SelectedRoomType(type))
                                expanded = false
                            },
                            text = { Text(text = type.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Monthly Payment (UGX)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = uiState.monthlyPayment ?: "",
                onValueChange = { addRoomViewModel.onEvent(AddRoomUiEvents.ChangedMonthlyPayment(it)) },
                placeholder = { Text(text = "Enter amount") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Room Number", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = uiState.roomNumber,
                onValueChange = { addRoomViewModel.onEvent(AddRoomUiEvents.ChangedRoomNumber(it)) },
                placeholder = { Text(text = "Enter room number") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.isOccupied,
                    onCheckedChange = { addRoomViewModel.onEvent(AddRoomUiEvents.CheckedOccupied(it)) }
                )
                Text(text = "Room is Occupied", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = {
                    if( !uiState.inserting ) {
                        onCancel()
                    }
                }) {
                    Text(text = "Cancel")
                }
                Button(
                    onClick = {
                        addRoomViewModel.onEvent(AddRoomUiEvents.SaveClicked)
                    },
                    modifier = Modifier.width(130.dp),
                    enabled = !uiState.monthlyPayment.isNullOrBlank() && uiState.monthlyPayment.toFloatOrNull() != null && uiState.roomNumber.isNotEmpty() && uiState.roomNumber.toIntOrNull() != null && !uiState.inserting
                ) {
                    if( uiState.inserting ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = if (roomId == null) "Save Room" else "Update Room")
                    }
                }
            }
        }
    }
}
