package com.engineerfred.easyrent.presentation.common

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
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface

@Composable
fun CustomAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    text1:String = "",
    text2:String = "",
    text3:String = "",
    boldText1: String = "",
    boldText2: String = "",
    confirmButtonText: String = "Delete",
    cancelButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        containerColor = MySecondary,
        title = { Text(
            text = title,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
        ) },
        text = {
            Text(
                buildAnnotatedString {
                    append(text1)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText1)
                    pop()
                    append(text2)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText2)
                    pop()
                    append(text3)
                },
                style = TextStyle(
                    fontSize = 19.sp,
                    color = MySurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    letterSpacing = 0.25.sp
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyError
                )
            ) {
                Text(confirmButtonText, color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                )
            ) {
                Text(cancelButtonText, color = Color.Black)
            }
        }
    )
}