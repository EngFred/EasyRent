package com.engineerfred.easyrent.presentation.screens.profile

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.engineerfred.easyrent.R.drawable
import com.engineerfred.easyrent.domain.modals.UserInfoUpdateStatus
import com.engineerfred.easyrent.util.formatCurrency
import com.engineerfred.easyrent.util.getCurrentMonthAndYear
import com.engineerfred.easyrent.util.getMonthlyPaymentsTotal
import com.engineerfred.easyrent.util.toFormattedDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun Profile(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onSignOutSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState = profileViewModel.uiState.collectAsState().value
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = uiState.signingOutSuccess) {
        if( uiState.signingOutSuccess ) {
            onSignOutSuccess()
        }
    }

    LaunchedEffect(key1 = uiState.isUpdating) {
        if ( uiState.isUpdating.not() ) {
            showBottomSheet = false
        }
    }

    LaunchedEffect(key1 = uiState.signOutErr) {
        if( uiState.signOutErr != null ) {
            Toast.makeText(context, uiState.signOutErr, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if( !uiState.signingOut ) {
                                onBack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    var ex by remember { mutableStateOf(false) }
                    Box{
                        IconButton(onClick = { ex = !ex }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = ex,
                            onDismissRequest = { ex = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Logout", fontSize = 18.sp)
                                        Spacer(Modifier.width(7.dp))
                                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                    }
                                },
                                onClick = {
                                    if ( uiState.signingOut.not() ) {
                                        ex = false
                                        profileViewModel.onEvent(ProfileUiEvents.LoggedOut)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Account", fontSize = 18.sp, color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    Toast.makeText(context, "Feature was not implemented!", Toast.LENGTH_SHORT).show()
                                    ex = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if(uiState.isLoading || !uiState.error.isNullOrEmpty()) Arrangement.Center else Arrangement.Top
        ) {

            when {
                uiState.isLoading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ){
                        CircularProgressIndicator()
                    }
                }

                !uiState.isLoading && uiState.error != null -> {
                    ErrorContainer(
                        error = uiState.error,
                        onRetry = { profileViewModel.onEvent(ProfileUiEvents.RetryClicked) }
                    )
                }

                else -> {
                    when {
                        uiState.user != null -> {
                            // Profile Image
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.3f))
                                    .clickable {
                                        profileViewModel.onEvent(
                                            ProfileUiEvents.ChangedUpdateState(
                                                UserInfoUpdateStatus.UpdatingProfileImage
                                            )
                                        )
                                        showBottomSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.user.imageUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = painterResource(id = drawable.default_profile_image1),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(120.dp) //80
                                            .clip(CircleShape)
                                            .border(2.dp, Color.Gray, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    GlideImage(
                                        model = uiState.user.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(120.dp) //80
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ) // Added border
                                            .background(Color.DarkGray),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // User Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    ProfileInfoRow(
                                        "Name:",
                                        "${uiState.user.firstName} ${uiState.user.lastName}",
                                        onClick = {
                                            profileViewModel.onEvent(ProfileUiEvents.ChangedUpdateState(UserInfoUpdateStatus.UpdatingNames))
                                            showBottomSheet = true
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Hostel:",
                                        uiState.user.hostelName ?: "N/A",
                                        onClick = {
                                            profileViewModel.onEvent(ProfileUiEvents.ChangedUpdateState(UserInfoUpdateStatus.UpdatingHostelName))
                                            showBottomSheet = true
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Email:",
                                        uiState.user.email,
                                        onClick = {
                                            Toast.makeText(context, "Update for emails currently not implemented! Try again later.", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Phone:",
                                        uiState.user.telNo,
                                        onClick = {
                                            profileViewModel.onEvent(ProfileUiEvents.ChangedUpdateState(UserInfoUpdateStatus.UpdatingPhoneNumber))
                                            showBottomSheet = true
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Joined:",
                                        uiState.user.createdAt.toFormattedDate(),
                                        onClick = {
                                            Toast.makeText(context, "This field can't be updated!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(26.dp))
                            if ( uiState.signingOut ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Signing out...")
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Green.copy(alpha = 0.1f)
                                )
                            ) {
                                val expectedIncome = uiState.rooms.sumOf { it.monthlyRent.toInt() }
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                ) {
                                    Text(getCurrentMonthAndYear(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold)
                                    Spacer(Modifier.size(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(fontWeight = FontWeight.Bold, text = "Total Income", textDecoration = TextDecoration.Underline)
                                            Spacer(Modifier.size(10.dp))
                                            Text(fontWeight = FontWeight.Bold, text = "UGX.${getMonthlyPaymentsTotal(uiState.payments)}")
                                        }
                                        Spacer(modifier = Modifier.size(15.dp))
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(fontWeight = FontWeight.Bold, text = "Expected Income", textDecoration = TextDecoration.Underline)
                                            Spacer(Modifier.size(10.dp))
                                            Text(fontWeight = FontWeight.Bold, text = "UGX.${formatCurrency(expectedIncome.toFloat())}")
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            ErrorContainer(
                                error = "User not found!",
                                errColor = Color.Gray,
                                onRetry = { profileViewModel.onEvent(ProfileUiEvents.RetryClicked)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sheet Dialog
        if (showBottomSheet) {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    profileViewModel.onEvent(ProfileUiEvents.SelectedNewProfileImage(uri.toString()))
                }
            }

            ModalBottomSheet(
                modifier = Modifier.imePadding(),
                onDismissRequest = { showBottomSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bottomSheetTitleTxt = if ( uiState.updateState == UserInfoUpdateStatus.UpdatingNames ) "Update Names" else if( uiState.updateState == UserInfoUpdateStatus.UpdatingHostelName ) "Update Hostel name" else if (uiState.updateState == UserInfoUpdateStatus.UpdatingPhoneNumber) "Update Phone number" else "Update Profile Picture"
                    Text(bottomSheetTitleTxt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    when( uiState.updateState ) {
                        is UserInfoUpdateStatus.UpdatingProfileImage -> {
                            Box(
                                modifier = Modifier.size(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.selectedImgUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = painterResource(id = drawable.default_profile_image1),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color.Gray, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    GlideImage(
                                        model = uiState.selectedImgUrl,
                                        contentDescription = "Selected Profile Picture",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color.Gray, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = { launcher.launch("image/*") },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color.Gray, CircleShape)
                                        .padding(4.dp),
                                    enabled = uiState.isUpdating.not()
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Image")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if ( uiState.selectedImgUrl !=  null ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Select an image!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = uiState.isUpdating.not()
                            ) {
                                if ( uiState.isUpdating ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Update profile image")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        is UserInfoUpdateStatus.UpdatingNames -> {
                            OutlinedTextField(
                                value = uiState.newFirstName,
                                onValueChange = { profileViewModel.onEvent(ProfileUiEvents.ChangedFirstName(it.trim())) },
                                label = { Text("First name") },
                                isError = uiState.firstNameErr != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None)
                            )
                            AnimatedVisibility(visible = uiState.firstNameErr != null) {
                                Text(uiState.firstNameErr ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.newLastName,
                                onValueChange = { profileViewModel.onEvent(ProfileUiEvents.ChangedLastName(it.trim())) },
                                label = { Text("Last name") },
                                isError = uiState.lastNameErr != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None)
                            )
                            AnimatedVisibility(visible = uiState.lastNameErr != null) {
                                Text(uiState.lastNameErr ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if ( uiState.firstNameErr == null && uiState.lastNameErr == null && uiState.newLastName.isNotEmpty() && uiState.newFirstName.isNotEmpty() ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Invalid form!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = uiState.isUpdating.not()
                            ) {
                                if ( uiState.isUpdating ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Update names")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        is UserInfoUpdateStatus.UpdatingHostelName -> {
                            OutlinedTextField(
                                value = uiState.newHostelName,
                                onValueChange = { profileViewModel.onEvent(ProfileUiEvents.ChangedHostelName(it.trim())) },
                                label = { Text("Hostel name") },
                                isError = uiState.hostelNameErr != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None)
                            )
                            AnimatedVisibility(visible = uiState.hostelNameErr != null) {
                                Text(uiState.hostelNameErr ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if ( uiState.hostelNameErr == null && uiState.newHostelName.isNotEmpty() ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Invalid form!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = uiState.isUpdating.not()
                            ) {
                                if ( uiState.isUpdating ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Update hostel Name")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        is UserInfoUpdateStatus.UpdatingPhoneNumber -> {
                            OutlinedTextField(
                                value = uiState.newPhoneNumber,
                                onValueChange = { profileViewModel.onEvent(ProfileUiEvents.ChangedPhoneNumber(it.trim())) },
                                label = { Text("Phone name") },
                                isError = uiState.phoneNameErr != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Phone
                                )
                            )
                            AnimatedVisibility(visible = uiState.phoneNameErr != null) {
                                Text(uiState.phoneNameErr ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if ( uiState.phoneNameErr == null && uiState.newPhoneNumber.isNotEmpty() ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Invalid form!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = uiState.isUpdating.not()
                            ) {
                                if ( uiState.isUpdating ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Update phone number")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        null -> Unit
                    }
                    Button(
                        onClick = { showBottomSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)

                    ) {
                        Text("Cancel", color = Color.Black)
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileInfoRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Text(text = value, fontSize = 20.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}


@Composable
fun ErrorContainer(
    modifier: Modifier = Modifier,
    error: String,
    onRetry: () -> Unit,
    errColor: Color = Color.Red
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            color = errColor,
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { onRetry() }) {
            Text("Retry")
        }
    }
}