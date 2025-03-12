package com.engineerfred.easyrent.presentation.screens.tenants.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.presentation.common.ConfirmTenantDeleteDialog
import com.engineerfred.easyrent.util.formatCurrency
import com.engineerfred.easyrent.util.toFormattedDate

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TenantItem(
    tenant: Tenant,
    onDelete: (tenant: Tenant) -> Unit,
    deletingTenant: () -> Boolean,
    deletedTenantId: String
) {
    val context = LocalContext.current
    val balanceColor = if (tenant.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336) // Green for positive, Red for negative
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    GlideImage(
                        model = tenant.profilePic,
                        contentDescription = "${tenant.name}'s profile picture",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) // Added border
                            .background(Color.DarkGray),
                        contentScale = ContentScale.Crop
                    )
                    SyncDot(isSynced = tenant.isSynced) // Placed inside profile picture
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tenant.name.replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Room ${tenant.roomNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Bal: UGX.${formatCurrency(tenant.balance)}",
                        fontWeight = FontWeight.Medium,
                        color = balanceColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column (
                    modifier = Modifier.height(70.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(
                            enabled = deletingTenant().not()
                        ) {
                            showDialog = !showDialog
                        },
                        textAlign = TextAlign.End
                    )

                    Text(
                        text = tenant.moveInDate.toFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }
            }
            if( deletingTenant() && tenant.id == deletedTenantId) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (showDialog) {
            ConfirmTenantDeleteDialog(
                tenant = tenant,
                onConfirm = {
                    onDelete(tenant)
                    showDialog = false
                },
                onDismiss = {
                    showDialog = false
                },
                roomNumber = tenant.roomNumber.toString()
            )
        }

    }
}

@Composable
fun SyncDot(
    modifier: Modifier = Modifier,
    isSynced: Boolean
) {
    val color = if (isSynced) Color(0xFF4CAF50) else Color(0xFFF44336) // Green for synced, Red for not synced
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White, CircleShape) // Adds a white border for better visibility
    )
}




