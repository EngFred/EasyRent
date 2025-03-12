package com.engineerfred.easyrent.presentation.screens.signup

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.engineerfred.easyrent.R

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onNavigateToSignIn: () -> Unit,
    onSignUpSuccess: () -> Unit,
    signUpViewModel: SignUpViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val uiState = signUpViewModel.uiState.collectAsState().value

    LaunchedEffect(key1 = uiState.signUpSuccessful) {
        if( uiState.signUpSuccessful ) {
            onSignUpSuccess()
        }
    }

    LaunchedEffect(key1 = uiState.signUpErr) {
        if( uiState.signUpErr != null ) {
            Toast.makeText(context, uiState.signUpErr, Toast.LENGTH_LONG).show()
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                signUpViewModel.onEvent(SignUpEvents.ImageUrlChangedChanged(it.toString()))
            }
        }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUrl.isNullOrEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_image1),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = uiState.imageUrl,
                            contentDescription = "Selected Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Change Image")
                    }
                }
                Spacer(modifier = Modifier.size(15.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.hostelName ?: "",
                        onValueChange = { signUpViewModel.onEvent(SignUpEvents.HostelNameChanged(it)) },
                        label = { Text("Hostel name (optional)") },
                        isError = !uiState.ck,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None)
                    )
                    AnimatedVisibility(visible = uiState.ck.not()) {
                        Text(uiState.hostelNameErr ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // First Name
            OutlinedTextField(
                value = uiState.firstName,
                onValueChange = { signUpViewModel.onEvent(SignUpEvents.FirstNameChanged(it)) },
                label = { Text("First Name") },
                singleLine = true,
                isError = uiState.firstNameErr != null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None)
            )
            AnimatedVisibility(uiState.firstNameErr != null) {
                Text(uiState.firstNameErr ?: "", color = MaterialTheme.colorScheme.error)
            }

            // Last Name
            OutlinedTextField(
                value = uiState.lastName,
                onValueChange = { signUpViewModel.onEvent(SignUpEvents.LastNameChanged(it)) },
                label = { Text("Last Name") },
                singleLine = true,
                isError = uiState.lastNameErr != null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None)
            )
            AnimatedVisibility(uiState.lastNameErr != null) {
                Text(uiState.lastNameErr ?: "", color = MaterialTheme.colorScheme.error)
            }

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { signUpViewModel.onEvent(SignUpEvents.EmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                isError = uiState.emailErr != null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email
                )
            )
            AnimatedVisibility(uiState.emailErr != null) {
                Text(uiState.emailErr ?: "", color = MaterialTheme.colorScheme.error)
            }

            // Phone Number
            OutlinedTextField(
                value = uiState.telNo,
                onValueChange = { signUpViewModel.onEvent(SignUpEvents.TelNoChanged(it)) },
                label = { Text("Phone Number") },
                isError = uiState.telNoErr != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone
                )
            )
            AnimatedVisibility(uiState.telNoErr != null) {
                Text(uiState.telNoErr ?: "", color = MaterialTheme.colorScheme.error)
            }

            // Password
            var passwordVisible by rememberSaveable { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { signUpViewModel.onEvent(SignUpEvents.PasswordChanged(it)) },
                label = { Text("Password") },
                isError = uiState.passwordErr != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password
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
            AnimatedVisibility(uiState.passwordErr != null) {
                Text(uiState.passwordErr ?: "", color = MaterialTheme.colorScheme.error)
            }

            // Sign Up Button
            Button(
                onClick = { signUpViewModel.onEvent(SignUpEvents.SignUpButtonClicked(context.contentResolver)) },
                enabled =  uiState.firstNameErr == null && uiState.firstName.isNotEmpty() && uiState.lastNameErr == null && uiState.lastName.isNotEmpty() && uiState.emailErr == null && uiState.email.isNotEmpty() && uiState.telNoErr == null && uiState.telNo.isNotEmpty() && uiState.passwordErr == null && uiState.password.isNotEmpty() && !uiState.signingUp && uiState.ck,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.signingUp) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Sign Up")
                }
            }

            // Navigate to Sign In
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account?")
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToSignIn) {
                    Text("Sign In")
                }
            }
        }
    }

    
}