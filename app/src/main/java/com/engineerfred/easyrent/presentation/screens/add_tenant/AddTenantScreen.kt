package com.engineerfred.easyrent.presentation.screens.add_tenant

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    tenantId: String?,
    roomId: String?,
    monthlyRent: String?,
    roomNumber: String?,
    modifier: Modifier = Modifier,
    onSaveSuccessFromRoom: () -> Unit,
    onSaveSuccessFromTenants: () -> Unit,
    addTenantViewModel: AddTenantViewModel = hiltViewModel()
) {

    val uiState = addTenantViewModel.uiState.collectAsState().value

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        if( monthlyRent != null ) {
            addTenantViewModel.onEvent(AddTenantUiEvents.BalanceChanged(monthlyRent))
        }
        if( roomNumber != null ) {
            addTenantViewModel.onEvent(AddTenantUiEvents.SelectedRoomNumber(roomNumber))
        }

        if ( roomId != null ) {
            addTenantViewModel.onEvent(AddTenantUiEvents.SelectedRoomId(roomId))
        }

        if( roomId == null && tenantId == null ) {
            addTenantViewModel.onEvent(AddTenantUiEvents.FetchedAvailableRooms)
        }

    }

    LaunchedEffect(key1 = uiState.saveSuccess) {
        if ( uiState.saveSuccess ) {
            if ( roomId != null ) onSaveSuccessFromRoom() else onSaveSuccessFromTenants()
        }
    }

    LaunchedEffect(key1 = uiState.insertionErr) {
        if( !uiState.insertionErr.isNullOrEmpty() ) {
            Toast.makeText(context, uiState.insertionErr, Toast.LENGTH_LONG).show()
        }
    }

    var roomDropdownExpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if ( uri == null ) {
            Toast.makeText(context, "No image selected!", Toast.LENGTH_LONG).show()
        } else {
            addTenantViewModel.onEvent(AddTenantUiEvents.ImageUrlChanged(uri.toString()))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (tenantId == null) "Add Tenant" else "Update Tenant", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))},
                navigationIcon = {
                    IconButton(onClick = { if ( roomId != null ) onSaveSuccessFromRoom() else onSaveSuccessFromTenants() }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Image Picker
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        if (!uiState.saving) {
                            launcher.launch("image/*")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imageUrl != null) {
                    AsyncImage(
                        model = uiState.imageUrl,
                        contentDescription = "Tenant Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AddAPhoto,
                        contentDescription = "Add Tenant Image",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            val enableButton = if ( roomId != null ) false else roomNumber.isNullOrEmpty() && uiState.availableRooms.isNotEmpty()
            // Room Selection Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { roomDropdownExpanded = !roomDropdownExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enableButton
                ) {
                    if( uiState.fetchingAvailableRooms ) {
                        CircularProgressIndicator()
                    } else {
                        val roomNumberBtnText = when {
                            uiState.roomNumber == null && uiState.availableRooms.isEmpty() && !uiState.fetchingAvailableRooms -> "No rooms available"
                            uiState.roomNumber != null -> "Room_${uiState.roomNumber}"
                            else -> "Select Room"
                        }
                        Text(
                            text =  roomNumberBtnText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                DropdownMenu(
                    expanded = roomDropdownExpanded,
                    onDismissRequest = { roomDropdownExpanded = false }
                ) {
                    uiState.availableRooms.forEach { room ->
                        DropdownMenuItem(
                            text = { Text("Room_${room.roomNumber}") },
                            onClick = {
                                //selectedRoom = room.name
                                addTenantViewModel.onEvent(AddTenantUiEvents.RoomSelected(room))
                                roomDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Name TextField
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.NameChanged(it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Email TextField
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.EmailChanged(it))  },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
            )

            // Balance TextField
            OutlinedTextField(
                value = uiState.balance,
                onValueChange = { },
                enabled = false,
                label = { Text("Balance (UGX)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            // Contact TextField
            OutlinedTextField(
                value = uiState.contact,
                onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.ContactChanged(it))  },
                label = { Text("Contact") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
            )

            // Emergency Contact TextField
            OutlinedTextField(
                value = uiState.emergencyContact ?: "",
                onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.EmergencyContactChanged(it)) },
                label = { Text("Emergency Contact") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
            )

            // ID Details TextField
            OutlinedTextField(
                value = uiState.idDetails ?: "",
                onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.ChangedIdDetails(it))  },
                label = { Text("ID Details") },
                modifier = Modifier.fillMaxWidth()
            )

            // Notes TextField
            OutlinedTextField(
                value = uiState.notes ?: "",
                onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.NotesChanged(it))  },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                singleLine = false
            )

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = {}) {
                    Text(text = "Cancel")
                }
                Button(
                    onClick = {
                        addTenantViewModel.onEvent(AddTenantUiEvents.SaveClicked(context.contentResolver))
                    },
                    modifier = Modifier.width(130.dp),
                    enabled = uiState.name.isNotEmpty() && uiState.balance.isNotEmpty() && uiState.contact.isNotEmpty() && uiState.selectedRoomId != null && !uiState.saving
                    //enabled = uiState..isNotBlank() && balance.isNotBlank() && contact.isNotBlank() && selectedRoom != null
                ) {
                    if( uiState.saving ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = if (tenantId == null) "Save Tenant" else "Update Tenant")
                    }
                }
            }
        }
    }
}

