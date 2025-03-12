package com.engineerfred.easyrent.presentation.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.engineerfred.easyrent.domain.modals.Tenant

@Composable
fun ConfirmTenantDeleteDialog(
    modifier: Modifier = Modifier,
    tenant: Tenant,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    roomNumber: String
) {
//    val backgroundColor = if (!isSystemInDarkTheme()) {
//        Color(0xFFFFFFFF) // Light theme background
//    } else {
//        Color(0xFF202124) // Dark theme background (Google's dark grey)
//    }

    val textColor = if (!isSystemInDarkTheme()) {
        Color.Black
    } else {
        Color.White
    }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        title = { Text(
            text = "Remove Tenant",
            color = textColor,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
        ) },
        text = {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to remove ")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(tenant.name)
                    pop()
                    append(" from Room ")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(roomNumber)
                    pop()
                    append("? This action cannot be undone.")
                },
                fontSize = 19.sp,
                color = textColor
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Yes, Remove", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}