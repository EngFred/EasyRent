package com.engineerfred.easyrent.presentation.screens.signin

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerfred.easyrent.presentation.common.CustomTextField
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    signInViewModel: SignInViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = signInViewModel.uiState.collectAsState().value

    LaunchedEffect(key1 = uiState.signInSuccessful) {
        if (uiState.signInSuccessful) onLoginSuccess()
    }

    LaunchedEffect(key1 = uiState.signInError) {
        uiState.signInError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
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
                Spacer(modifier = Modifier.height(100.dp))
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold, color = Color.White
                    )
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

                        CustomTextField(
                            value = uiState.email,
                            onValueChange = { signInViewModel.onEvent(SignInUIEvents.EmailChanged(it)) },
                            label = "Email",
                            errorMessage = uiState.emailError,
                            keyboardType = KeyboardType.Email
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomTextField(
                            value = uiState.password,
                            onValueChange = { signInViewModel.onEvent(SignInUIEvents.PasswordChanged(it)) },
                            label = "Password",
                            isAuth = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { signInViewModel.onEvent(SignInUIEvents.SignInButtonClicked) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.emailError == null && uiState.email.isNotEmpty() && uiState.password.isNotEmpty() && !uiState.signingIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyPrimary,
                                disabledContainerColor = MyPrimary
                            )
                        ) {
                            if (uiState.signingIn) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Don't have an account?", color = MySurface)
                            TextButton(onClick = onNavigateToSignUp) {
                                Text("Sign Up", color = MyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
