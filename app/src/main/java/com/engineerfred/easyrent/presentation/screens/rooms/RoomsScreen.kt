package com.engineerfred.easyrent.presentation.screens.rooms

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkManager
import com.engineerfred.easyrent.presentation.common.CustomSyncToast
import com.engineerfred.easyrent.presentation.screens.rooms.components.DrawerContent
import com.engineerfred.easyrent.presentation.screens.rooms.components.RoomItem
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary
import com.engineerfred.easyrent.util.WorkerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
    workManager: WorkManager,
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

    DisposableEffect(Unit) {
        onDispose {
            roomsViewModel.hideSyncButton()
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
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Rooms")
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MySecondary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
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
                Column(
                    modifier = Modifier.padding(bottom = 36.dp, end = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(visible = uiState.showSyncButton ) {
                        IconButton(
                            onClick = {
                                WorkerUtils.syncRoomsImmediately(workManager)
                                roomsViewModel.hideSyncButton()
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
                            onAddRoom()
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
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary))),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                when {
                    uiState.loading -> {
                        CircularProgressIndicator(
                            color = Color.White
                        )
                    }
                    uiState.error != null && !uiState.loading -> {
                        Text(
                            text = uiState.error,
                            style = TextStyle(
                                color = MyError,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    blurRadius = 6f,
                                    offset = Offset(2f, 2f)
                                )
                            )
                        )
                    }
                    else -> {
                        when {
                            uiState.rooms.isEmpty() -> {
                                Text(
                                    text = "No rooms yet!",
                                    style = TextStyle(
                                        color = MySurface,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            blurRadius = 6f,
                                            offset = Offset(2f, 2f)
                                        )
                                    )
                                )
                            }
                            else -> {
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
                                    CustomSyncToast(
                                        showSyncRequired = uiState.showSyncRequired,
                                        dataCount = uiState.unSyncedRooms.size,
                                        dataName = "room"
                                    )
                                    Box(Modifier
                                        .fillMaxWidth().padding(bottom = 16.dp)
                                        .align(Alignment.BottomCenter), contentAlignment = Alignment.Center){
                                        Text(
                                            "Long press on the room to delete!",
                                            color = Color.Cyan,
                                            style = TextStyle(
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
    }
}

private fun toggleDrawerState(scope: CoroutineScope, drawerState: DrawerState) {
    scope.launch {
        drawerState.apply {
            if(isOpen) close() else open()
        }
    }
}