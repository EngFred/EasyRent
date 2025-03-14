package com.engineerfred.easyrent.presentation.screens.signup

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.engineerfred.easyrent.R
import com.engineerfred.easyrent.presentation.common.CustomTextField
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@Composable
fun SignUpScreen(
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

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { signUpViewModel.onEvent(SignUpEvents.ImageUrlChangedChanged(it.toString())) }
        }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
                .padding(it)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create an Account",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MyCardBg),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = uiState.imageUrl ?: R.drawable.default_profile_image1,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.Gray, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            FloatingActionButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.size(32.dp),
                                containerColor = MyPrimary,
                                contentColor = Color.White
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Change Image", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Input Fields
                        CustomTextField(
                            value = uiState.firstName,
                            onValueChange = { signUpViewModel.onEvent(SignUpEvents.FirstNameChanged(it)) },
                            label = "First Name",
                            keyboardType = KeyboardType.Text,
                            errorMessage = uiState.firstNameErr
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomTextField(
                            value = uiState.lastName,
                            keyboardType = KeyboardType.Text,
                            onValueChange = { signUpViewModel.onEvent(SignUpEvents.LastNameChanged(it)) },
                            label = "Last Name",
                            errorMessage = uiState.lastNameErr
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomTextField(
                            value = uiState.email,
                            keyboardType = KeyboardType.Email,
                            onValueChange = { signUpViewModel.onEvent(SignUpEvents.EmailChanged(it)) },
                            label = "Email",
                            errorMessage = uiState.emailErr
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomTextField(
                            value = uiState.telNo,
                            keyboardType = KeyboardType.Phone,
                            onValueChange = { signUpViewModel.onEvent(SignUpEvents.TelNoChanged(it)) },
                            label = "Tel No",
                            errorMessage = uiState.telNoErr
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomTextField(
                            value = uiState.hostelName ?: "",
                            keyboardType = KeyboardType.Text,
                            onValueChange = { signUpViewModel.onEvent(SignUpEvents.HostelNameChanged(it)) },
                            label = "Hostel Name (optional)",
                            errorMessage = uiState.hostelNameErr
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomTextField(
                            value = uiState.password,
                            onValueChange = { signUpViewModel.onEvent(SignUpEvents.PasswordChanged(it)) },
                            label = "Password",
                            errorMessage = uiState.passwordErr,
                            isAuth = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Sign Up Button
                        Button(
                            onClick = { signUpViewModel.onEvent(SignUpEvents.SignUpButtonClicked(context.contentResolver)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled =  uiState.firstNameErr == null && uiState.firstName.isNotEmpty() && uiState.lastNameErr == null && uiState.lastName.isNotEmpty() && uiState.emailErr == null && uiState.email.isNotEmpty() && uiState.telNoErr == null && uiState.telNo.isNotEmpty() && uiState.passwordErr == null && uiState.password.isNotEmpty() && !uiState.signingUp && uiState.ck,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyPrimary,
                                disabledContainerColor = MyPrimary,
                            )
                        ) {
                            if (uiState.signingUp) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        // Navigate to Sign In
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Already have an account?", color = MySurface)
                            TextButton(onClick = onNavigateToSignIn) {
                                Text("Sign In", color = MyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
