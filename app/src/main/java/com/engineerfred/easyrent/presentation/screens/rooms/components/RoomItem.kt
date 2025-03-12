package com.engineerfred.easyrent.presentation.screens.rooms.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.presentation.common.SyncStatus
import com.engineerfred.easyrent.util.formatCurrency
import com.engineerfred.easyrent.util.toFormattedDate
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomItem(
    room: Room,
    tenant: Tenant?,
    isDeletingTenant: Boolean,
    deleteSuccessful: Boolean,
    onAddTenantClick: (room: Room) -> Unit,
    onClick: (String) -> Unit,
    onDeleteTenant: (tenant: Tenant, roomId: String) -> Unit,
    onDeleteRoom: (room: Room) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDeleteRoomDialog by remember { mutableStateOf(false) }
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
                        showConfirmDeleteRoomDialog = true
                    } else {
                        Toast.makeText(context, "Can't delete an occupied room!", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = "Type: ${room.roomType.replaceFirstChar { it.uppercase() }}", fontSize = 16.sp)
                    Text(text = "Rent: UGX.${formatCurrency(room.monthlyRent)}", fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (room.isOccupied) "Occupied" else "Available",
                        color = if (room.isOccupied) Color.Red else Color.Magenta,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    SyncStatus(isSynced = room.isSynced)
                }
            }

            var showDeleteTenantDialog by remember { mutableStateOf(false) }
            AnimatedVisibility(visible = expanded && tenant != null && room.id == tenant.roomId) {
                val bgColor = if (!isSystemInDarkTheme()) Color(0xFF81D4FA) else Color(0xFF37474F)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tenant Info",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline
                    )
                    Text(text = "Name: ${tenant?.name?.capitalize(Locale.ROOT)}")
                    Text(text = "Contact: ${tenant?.contact}")
                    Text(text = "Email: ${tenant?.email ?: "N/A"}")
                    Text(text = "Balance: UGX ${formatCurrency(tenant?.balance ?: 0F)}")
                    Text(text = "Move-in Date: ${tenant?.moveInDate?.toFormattedDate()}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            tenant?.let {
                                onDeleteTenant(it, it.roomId)
                                showDeleteTenantDialog = false
                                expanded = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
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

    if (showConfirmDeleteRoomDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteRoomDialog = false },
            title = { Text(text = "Delete Room", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(buildAnnotatedString {
                    append("Are you sure you want to delete ")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append("ROOM ${room.roomNumber}? ")
                    pop()
                    append("This action cannot be undone.")
                })
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteRoomDialog = false
                        onDeleteRoom(room)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmDeleteRoomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

