package com.engineerfred.easyrent.presentation.screens.tenants.components

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.engineerfred.easyrent.R
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.presentation.common.CustomAlertDialog
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.util.formatCurrency
import com.engineerfred.easyrent.util.toFormattedDate

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TenantItem(
    tenant: Tenant,
    onDelete: (tenant: Tenant) -> Unit,
    onImageClicked: (String) -> Unit,
    deletingTenant: () -> Boolean,
    deletedTenantId: String
) {
    val balanceColor = if (tenant.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MyCardBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if( tenant.profilePic != null ) {
                        Log.d("TenantItem", "Tenant profile pic: ${tenant.profilePic}")
                        GlideImage(
                            model = tenant.profilePic,
                            contentDescription = "${tenant.name}'s profile picture",
                            modifier = Modifier
                                .clickable { onImageClicked(tenant.profilePic) }
                                .size(70.dp)
                                .clip(CircleShape)
                                .border(2.dp, MyPrimary, CircleShape), // Added border
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.default_profile_image1),
                            contentDescription = "${tenant.name}'s profile picture",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .border(2.dp, MyPrimary, CircleShape)
                                .background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                    }
                    SyncDot(isSynced = tenant.isSynced)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tenant.name.replaceFirstChar { it.uppercase() },
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Room ${tenant.roomNumber}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MySurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Bal: UGX.${formatCurrency(tenant.balance)}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = balanceColor,
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
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
                        maxLines = 1,
                        modifier = Modifier.clickable(
                            enabled = deletingTenant().not()
                        ) {
                            showDialog = !showDialog
                        },
                        style = TextStyle(
                            fontSize = 14.sp,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Medium,
                            color = MyError,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = .5f),
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
                    )

                    Text(
                        text = tenant.moveInDate.toFormattedDate(),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MySurface
                        )
                    )
                }
            }
            if( deletingTenant() && tenant.id == deletedTenantId) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                }
            }
        }

        if (showDialog) {
            CustomAlertDialog(
                title = "Remove Tenant",
                text1 = "Are you sure you want to remove ",
                boldText1 = tenant.name,
                text2 = " from Room ",
                boldText2 = tenant.roomNumber.toString(),
                text3 = "? This action cannot be undone.",
                confirmButtonText = "Yes, Remove",
                onConfirm = {
                    onDelete(tenant)
                    showDialog = false
                },
                onDismiss = {
                    showDialog = false
                },
            )
        }

    }
}

@Composable
fun SyncDot(
    modifier: Modifier = Modifier,
    isSynced: Boolean
) {
    val color = if (isSynced) Color(0xFF4CAF50) else Color(0xFFF44336)
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White, CircleShape)
    )
}




