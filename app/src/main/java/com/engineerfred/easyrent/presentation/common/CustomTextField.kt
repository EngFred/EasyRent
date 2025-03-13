package com.engineerfred.easyrent.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MySurface

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    errorMessage: String? = null,
    isAuth: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {

    Column(modifier = modifier.fillMaxWidth()) {
        if( !isAuth ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, color = MySurface) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MySurface,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = MySurface,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = MySurface,
                    focusedTextColor = MySurface,
                    unfocusedTextColor = MySurface,
                    errorBorderColor = MyError,
                    errorLabelColor = MyError,
                    errorCursorColor = MyError,
                    errorTextColor = MyError,
                    disabledBorderColor = MySurface,
                    disabledTextColor = MySurface
                )
            )
        } else {
            var passwordVisible by rememberSaveable { mutableStateOf(false) }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, color = MySurface) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MySurface,
                    focusedLabelColor =  MySurface,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = MySurface,
                    focusedTrailingIconColor = MySurface,
                    unfocusedTrailingIconColor = MySurface,
                    focusedTextColor = MySurface,
                    unfocusedTextColor = MySurface,
                    errorBorderColor = MyError,
                    errorLabelColor = MyError,
                    errorCursorColor = MyError,
                    errorTrailingIconColor = MyError,
                    errorTextColor = MyError,
                    errorLeadingIconColor = MyError,
                    disabledBorderColor = MySurface,
                    disabledTextColor = MySurface
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                }
            )
        }
        AnimatedVisibility(
            visible = errorMessage != null
        ) {
            Text(
                text = errorMessage ?: "",
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = MyError,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        blurRadius = 2f
                    )
                )
            )
        }
    }
}