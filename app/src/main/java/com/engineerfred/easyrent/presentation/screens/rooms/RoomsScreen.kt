package com.engineerfred.easyrent.presentation.screens.rooms

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
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
import com.engineerfred.easyrent.presentation.screens.rooms.components.DrawerContent
import com.engineerfred.easyrent.presentation.screens.rooms.components.RoomItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    modifier: Modifier = Modifier,
    onAddRoom: () -> Unit,
    onPaymentsClicked: () -> Unit,
    onExpensesClicked: () -> Unit,
    onTenantsClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onAddTenant: (roomId: String, monthlyRent: String, roomNumber: String) -> Unit,
    roomsViewModel: RoomsViewModel = hiltViewModel()
) {

    val uiState = roomsViewModel.uiState.collectAsState().value
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(uiState.deletingRoomErr) {
        if ( uiState.deletingRoomErr != null ) {
            Toast.makeText(context, uiState.deletingRoomErr, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.deletingTenantErr) {
        if ( uiState.deletingTenantErr != null ) {
            Toast.makeText(context, uiState.deletingTenantErr, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.deletingRoom) {
        if ( uiState.deletingRoom ) {
            Toast.makeText(context, "Deleting room...", Toast.LENGTH_LONG).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    onPaymentsClicked = {
                        toggleDrawerState(scope, drawerState)
                        onPaymentsClicked()
                    },
                    onExpensesClicked = {
                        toggleDrawerState(scope, drawerState)
                        onExpensesClicked()
                    },
                    onTenantsClicked = {
                        toggleDrawerState(scope, drawerState)
                        onTenantsClicked()
                    },
                    onProfileClicked = {
                        toggleDrawerState(scope, drawerState)
                        onSettingsClicked()
                    }
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier,
            topBar = {
                TopAppBar(title = { Text(text = "Rooms") },
                    navigationIcon = {
                    IconButton(
                        onClick = {
                            toggleDrawerState(scope, drawerState)
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.MenuOpen, contentDescription = null)
                    }
                })
            },

            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddRoom,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                when {
                    uiState.loading -> {
                        CircularProgressIndicator()
                    }
                    uiState.error != null && !uiState.loading -> {
                        Text(text = uiState.error, style = TextStyle(color = Color.Red, fontWeight = FontWeight.W200, textAlign = TextAlign.Center),fontSize = 18.sp)
                    }
                    else -> {
                        when {
                            uiState.rooms.isEmpty() -> {
                                Text(text = "No rooms yet!", style = TextStyle(color = Color.Gray, fontWeight = FontWeight.W200, textAlign = TextAlign.Center, fontSize = 20.sp))
                            }
                            else -> {
                                //Text(text = "Found ${uiState.rooms.size} rooms!", style = TextStyle(color = Color.Gray, fontWeight = FontWeight.W200, textAlign = TextAlign.Center, fontSize = 30.sp))
                                Box(Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        items(count = uiState.rooms.size, key = { uiState.rooms[it].id }) {
                                            RoomItem(
                                                room = uiState.rooms[it],
                                                tenant = uiState.tenant,
                                                onAddTenantClick = { room ->
                                                    onAddTenant(room.id, room.monthlyRent.toString(), room.roomNumber.toString())
                                                },
                                                onDeleteTenant = { tenant, roomId ->
                                                    roomsViewModel.onEvent(RoomsEvents.TenantDeleted(tenant, roomId))
                                                },
                                                isDeletingTenant = uiState.isDeletingTenant,
                                                deleteSuccessful = uiState.deleteTenantSuccessful,
                                                onDeleteRoom = { room ->
                                                    if ( uiState.deletingRoom.not() ) {
                                                        roomsViewModel.onEvent(RoomsEvents.RoomDeleted(room))
                                                    }
                                                },
                                                onClick = { roomId ->
                                                    roomsViewModel.onEvent(RoomsEvents.RoomSelected(roomId))
                                                }
                                            )
                                        }
                                    }
                                    Box(Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter), contentAlignment = Alignment.Center){
                                        Text(
                                            "Long press on the room to delete!",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = Color.Magenta
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
}

private fun toggleDrawerState(scope: CoroutineScope, drawerState: DrawerState) {
    scope.launch {
        drawerState.apply {
            if(isOpen) close() else open()
        }
    }
}