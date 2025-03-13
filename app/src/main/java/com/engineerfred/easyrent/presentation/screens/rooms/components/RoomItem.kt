package com.engineerfred.easyrent.presentation.screens.rooms.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.presentation.common.CustomAlertDialog
import com.engineerfred.easyrent.presentation.common.SyncStatus
import com.engineerfred.easyrent.presentation.theme.LightSkyBlue
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary
import com.engineerfred.easyrent.util.formatCurrency
import com.engineerfred.easyrent.util.toFormattedDate
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomItem(
    room: Room,
    tenant: Tenant?,
    isDeletingTenant: Boolean,
    onAddTenantClick: (room: Room) -> Unit,
    onClick: (String) -> Unit,
    onDeleteTenant: (tenant: Tenant, roomId: String) -> Unit,
    onDeleteRoom: (room: Room) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteRoomDialog by remember { mutableStateOf(false) }
    var showDeleteTenantDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    if (room.isOccupied) {
                        onClick(room.id)
                        expanded = !expanded
                    } else {
                        onAddTenantClick(room)
                    }
                },
                onLongClick = {
                    if (!room.isOccupied) {
                        showDeleteRoomDialog = true
                    } else {
                        Toast.makeText(context, "Can't delete an occupied room!", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.background(MyCardBg).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Room ${room.roomNumber}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Type: ${room.roomType.replaceFirstChar { it.uppercase() }}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MySurface,
                            fontWeight = FontWeight.Medium,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rent: UGX.${formatCurrency(room.monthlyRent)}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MySurface,
                            fontWeight = FontWeight.Medium,
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (room.isOccupied) "Occupied" else "Available",
                        color = if (room.isOccupied) MyPrimary else Color.Magenta,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MySurface,
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
                    )
                    SyncStatus(isSynced = room.isSynced)
                }
            }

            if( expanded ) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            AnimatedVisibility(visible = expanded && tenant != null && room.id == tenant.roomId) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(LightSkyBlue, MyTertiary)), shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tenant Info",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        color = MySurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Name: ${tenant?.name?.capitalize(Locale.ROOT)}", color = MySurface)
                    Text(text = "Contact: ${tenant?.contact}", color = MySurface)
                    Text(text = "Email: ${tenant?.email ?: "N/A"}", color = MySurface)
                    Text(text = "Balance: UGX ${formatCurrency(tenant?.balance ?: 0F)}", color = MySurface)
                    Text(text = "Move-in Date: ${tenant?.moveInDate?.toFormattedDate()}", color = MySurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showDeleteTenantDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MyError),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isDeletingTenant) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Remove Tenant", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteRoomDialog) {
        CustomAlertDialog(
            title = "Delete Room",
            text1 = "Are you sure you want to delete ",
            boldText1 = "ROOM ${room.roomNumber}? ",
            text2 = "This action cannot be undone.",
            confirmButtonText = "Yes, Delete",
            onConfirm = {
                showDeleteRoomDialog = false
                onDeleteRoom(room)
            },
            onDismiss = { showDeleteRoomDialog = false }
        )
    }

    if ( showDeleteTenantDialog ) {
        CustomAlertDialog(
            title = "Delete Tenant",
            text1 = "Are you sure you want to remove ",
            boldText1 = "${tenant?.name?.capitalize(Locale.ROOT)}? ",
            text2 = "from ",
            boldText2 = "ROOM ${room.roomNumber}? ",
            text3 = "This action cannot be undone.",
            confirmButtonText = "Yes, Remove",
            onConfirm = {
                showDeleteTenantDialog = false
                tenant?.let { onDeleteTenant(it, it.roomId) }
                expanded = false
            },
            onDismiss = { showDeleteTenantDialog = false }
        )
    }
}

