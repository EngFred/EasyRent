package com.engineerfred.easyrent.presentation.screens.add_tenant

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.engineerfred.easyrent.R
import com.engineerfred.easyrent.presentation.common.CustomTextField
import com.engineerfred.easyrent.presentation.theme.LightSkyBlue
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    tenantId: String?,
    roomId: String?,
    monthlyRent: String?,
    roomNumber: String?,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MySecondary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
                .padding(paddingValues)
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Image Picker
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
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
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_image1),
                            contentDescription = "Add Tenant Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                val enableButton = if ( roomId != null ) false else roomNumber.isNullOrEmpty() && uiState.availableRooms.isNotEmpty()
                // Room Selection Dropdown
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = { roomDropdownExpanded = !roomDropdownExpanded },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MyPrimary,
                            disabledContentColor = MyPrimary,
                            disabledContainerColor = Color.Transparent,
                            containerColor = Color.Transparent
                        ),
                        enabled = enableButton
                    ) {
                        if( uiState.fetchingAvailableRooms ) {
                            CircularProgressIndicator(
                                color = Color.White
                            )
                        } else {
                            val roomNumberBtnText = when {
                                uiState.roomNumber == null && uiState.availableRooms.isEmpty() -> "No rooms available"
                                uiState.roomNumber != null -> "Room_${uiState.roomNumber}"
                                else -> "Select Room"
                            }
                            Text(
                                text =  roomNumberBtnText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    DropdownMenu(
                        expanded = roomDropdownExpanded,
                        modifier = Modifier.background(Brush.horizontalGradient(listOf(MyTertiary, LightSkyBlue))),
                        onDismissRequest = { roomDropdownExpanded = false }
                    ) {
                        uiState.availableRooms.forEach { room ->
                            DropdownMenuItem(
                                text = { Text("Room_${room.roomNumber}", color = MySurface) },
                                onClick = {
                                    addTenantViewModel.onEvent(AddTenantUiEvents.RoomSelected(room))
                                    roomDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                //TextFields
                CustomTextField(
                    value = uiState.name,
                    onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.NameChanged(it)) },
                    label = "Name",
                    errorMessage = uiState.nameErr
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    value = uiState.email,
                    onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.EmailChanged(it))  },
                    label = "Email",
                    errorMessage = uiState.emailErr,
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    value = uiState.balance,
                    onValueChange = { },
                    enabled = false,
                    label = "Balance (UGX)",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    value = uiState.contact,
                    onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.ContactChanged(it))  },
                    label = "Contact",
                    errorMessage = uiState.contactErr,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    value = uiState.emergencyContact ?: "",
                    onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.EmergencyContactChanged(it)) },
                    label = "Emergency Contact (Optional)",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    value = uiState.idDetails ?: "",
                    onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.ChangedIdDetails(it))  },
                    label = "ID Details (Optional)",
                )

                Spacer(Modifier.height(8.dp))

                // Notes TextField
                CustomTextField(
                    modifier = Modifier.imePadding(),
                    value = uiState.notes ?: "",
                    onValueChange = { addTenantViewModel.onEvent(AddTenantUiEvents.NotesChanged(it))  },
                    label ="Additional Notes (Optional)",
                )
            }

            Spacer(Modifier.height(8.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        if( !uiState.saving ) {
                            if ( roomId != null ) onSaveSuccessFromRoom() else onSaveSuccessFromTenants()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MyPrimary,
                        containerColor = Color.Transparent
                    )
                ) {
                    Text(text = "Cancel")
                }
                Button(
                    onClick = {
                        addTenantViewModel.onEvent(AddTenantUiEvents.SaveClicked(context.contentResolver))
                    },
                    modifier = Modifier.width(130.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = MyPrimary,
                        disabledContainerColor = MyPrimary
                    ),
                    enabled = uiState.nameErr == null && uiState.name.isNotEmpty() && uiState.balance.isNotEmpty() && uiState.contactErr == null && uiState.contact.isNotEmpty() && uiState.emailErr == null && uiState.email.isNotEmpty() && uiState.selectedRoomId != null && !uiState.saving
                ) {
                    if( uiState.saving ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(text = if (tenantId == null) "Save Tenant" else "Update Tenant")
                    }
                }
            }
        }
    }
}

