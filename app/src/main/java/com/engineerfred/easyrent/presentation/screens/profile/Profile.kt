package com.engineerfred.easyrent.presentation.screens.profile

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.engineerfred.easyrent.R.drawable
import com.engineerfred.easyrent.domain.modals.UserInfoUpdateStatus
import com.engineerfred.easyrent.presentation.common.CustomTextField
import com.engineerfred.easyrent.presentation.common.CustomAlertDialog
import com.engineerfred.easyrent.presentation.screens.profile.components.CustomUpdateButton
import com.engineerfred.easyrent.presentation.screens.profile.components.ErrorContainer
import com.engineerfred.easyrent.presentation.screens.profile.components.ProfileInfoRow
import com.engineerfred.easyrent.presentation.theme.LightSkyBlue
import com.engineerfred.easyrent.presentation.theme.MyCardBg
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary
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
    onImageClicked: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState = profileViewModel.uiState.collectAsState().value
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var showConfirmLogoutDialog by remember { mutableStateOf(false) }

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
                    containerColor = MySecondary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
                        IconButton(onClick = {
                            if( uiState.signingOut.not() ) {
                                ex = !ex
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = ex,
                            onDismissRequest = { ex = false },
                            modifier = Modifier.background(
                                Brush.horizontalGradient(listOf(
                                    LightSkyBlue, MyTertiary
                                )))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Logout",
                                            fontSize = 18.sp,
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                color = MySurface,
                                                shadow = Shadow(
                                                    color = Color.Black,
                                                    blurRadius = 3f
                                                )
                                            )
                                        )
                                        Spacer(Modifier.width(7.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = MySurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    if ( uiState.signingOut.not() ) {
                                        ex = false
                                        showConfirmLogoutDialog = true
                                    }
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(
                                    "Delete Account",
                                    fontSize = 18.sp,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red.copy(alpha = .5f),
                                        shadow = Shadow(
                                            color = Color.White,
                                            blurRadius = 3f
                                        )
                                    )
                                ) },
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
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
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
                        CircularProgressIndicator(
                            color = Color.White
                        )
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
                            Text(
                                "Tap to edit!",
                                modifier = Modifier.fillMaxWidth(),
                                style = TextStyle(
                                    textAlign = TextAlign.Start,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MyPrimary,
                                    shadow = Shadow(
                                        color = Color.White.copy(alpha = .5f),
                                        blurRadius = 6f,
                                        offset = Offset(3f,3f)
                                    )
                                )
                            )
                            Spacer(Modifier.size(16.dp))
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
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MyPrimary, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    GlideImage(
                                        model = uiState.user.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                MyPrimary,
                                                CircleShape
                                            ),
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
                                Column(
                                    modifier = Modifier.fillMaxWidth().background(MyCardBg).padding(vertical = 16.dp)
                                ) {
                                    ProfileInfoRow(
                                        "Name:",
                                        "${uiState.user.firstName.replaceFirstChar { it.uppercase() }} ${uiState.user.lastName.replaceFirstChar { it.uppercase() }}",
                                        onClick = {
                                            if( uiState.signingOut.not() ) {
                                                profileViewModel.onEvent(ProfileUiEvents.ChangedUpdateState(UserInfoUpdateStatus.UpdatingNames))
                                                showBottomSheet = true
                                            }
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Hostel:",
                                        uiState.user.hostelName ?: "N/A",
                                        onClick = {
                                            if(  uiState.signingOut.not() ) {
                                                profileViewModel.onEvent(ProfileUiEvents.ChangedUpdateState(UserInfoUpdateStatus.UpdatingHostelName))
                                                showBottomSheet = true
                                            }
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Email:",
                                        uiState.user.email,
                                        onClick = {
                                            if( uiState.signingOut.not() ) {
                                                Toast.makeText(context, "Can't update email!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Phone:",
                                        uiState.user.telNo,
                                        onClick = {
                                            if( uiState.signingOut.not()  ) {
                                                profileViewModel.onEvent(ProfileUiEvents.ChangedUpdateState(UserInfoUpdateStatus.UpdatingPhoneNumber))
                                                showBottomSheet = true
                                            }
                                        }
                                    )
                                    ProfileInfoRow(
                                        "Joined:",
                                        uiState.user.createdAt.toFormattedDate(),
                                        onClick = {
                                            if( uiState.signingOut.not() ) {
                                                Toast.makeText(context, "This field can't be updated!", Toast.LENGTH_SHORT).show()
                                            }
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
                                        modifier = Modifier.size(80.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Signing out...", color = MySurface)
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
                                    modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(MyTertiary, LightSkyBlue))).padding(20.dp),
                                ) {
                                    Text(
                                        getCurrentMonthAndYear(),
                                        modifier = Modifier.fillMaxWidth(),
                                        style = TextStyle(
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = MySurface,
                                            shadow = Shadow(
                                                color = Color.Black,
                                                blurRadius = 3f
                                            )
                                        )
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                fontWeight = FontWeight.Bold,
                                                text = "Total Income",
                                                fontSize = 20.sp,
                                                textDecoration = TextDecoration.Underline,
                                                color = Color.White
                                            )
                                            Spacer(Modifier.size(10.dp))
                                            Text(
                                                fontWeight = FontWeight.Bold,
                                                text = "UGX.${getMonthlyPaymentsTotal(uiState.payments)}",
                                                fontSize = 20.sp,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.size(15.dp))
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                fontWeight = FontWeight.Bold,
                                                text = "Expected Income",
                                                fontSize = 20.sp,
                                                textDecoration = TextDecoration.Underline,
                                                color = Color.White
                                            )
                                            Spacer(Modifier.size(10.dp))
                                            Text(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                text = "UGX.${formatCurrency(expectedIncome.toFloat())}",
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            ErrorContainer(
                                error = "User not found!",
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
                onDismissRequest = { showBottomSheet = false },
                containerColor = MyCardBg,
                contentColor = MySurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bottomSheetTitleTxt = if ( uiState.updateState == UserInfoUpdateStatus.UpdatingNames ) "Update Names" else if( uiState.updateState == UserInfoUpdateStatus.UpdatingHostelName ) "Update Hostel name" else if (uiState.updateState == UserInfoUpdateStatus.UpdatingPhoneNumber) "Update Phone number" else "Update Profile Picture"
                    Text(
                        bottomSheetTitleTxt,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 6f
                            )
                        ),
                    )
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
                                            .clickable {
                                                if( uiState.isUpdating.not() ) {
                                                    showBottomSheet = false
                                                    onImageClicked(uiState.selectedImgUrl)
                                                }
                                            }
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
                                        .background(MyPrimary, CircleShape)
                                        .border(1.dp, MyPrimary, CircleShape)
                                        .padding(4.dp),
                                    enabled = uiState.isUpdating.not()
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Image")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            CustomUpdateButton(
                                onClick = {
                                    if ( uiState.selectedImgUrl !=  null ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Select an image!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                text = "Update profile image",
                                enabled = uiState.isUpdating.not() && uiState.signingOut.not() ,
                                updating = uiState.isUpdating
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        is UserInfoUpdateStatus.UpdatingNames -> {
                            CustomTextField(
                                value = uiState.newFirstName,
                                onValueChange = {
                                    profileViewModel.onEvent(
                                        ProfileUiEvents.ChangedFirstName(
                                            it.trim()
                                        )
                                    )
                                },
                                label = "First name",
                                errorMessage = uiState.firstNameErr,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomTextField(
                                value = uiState.newLastName,
                                onValueChange = {
                                    profileViewModel.onEvent(
                                        ProfileUiEvents.ChangedLastName(
                                            it.trim()
                                        )
                                    )
                                },
                                label = "Last name",
                                errorMessage = uiState.lastNameErr
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CustomUpdateButton(
                                onClick = {
                                    if ( uiState.firstNameErr == null && uiState.lastNameErr == null && uiState.newLastName.isNotEmpty() && uiState.newFirstName.isNotEmpty() ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Invalid form!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                text = "Update names",
                                enabled = uiState.isUpdating.not() && uiState.signingOut.not() ,
                                updating = uiState.isUpdating
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        is UserInfoUpdateStatus.UpdatingHostelName -> {
                            CustomTextField(
                                value = uiState.newHostelName,
                                onValueChange = { profileViewModel.onEvent(ProfileUiEvents.ChangedHostelName(it.trim())) },
                                label = "Hostel name",
                                errorMessage = uiState.hostelNameErr,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CustomUpdateButton(
                                onClick = {
                                    if ( uiState.hostelNameErr == null && uiState.newHostelName.isNotEmpty() ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Invalid form!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                text = "Update hostel Name",
                                enabled = uiState.isUpdating.not() && uiState.signingOut.not() ,
                                updating = uiState.isUpdating
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        is UserInfoUpdateStatus.UpdatingPhoneNumber -> {
                            CustomTextField(
                                value = uiState.newPhoneNumber,
                                onValueChange = { profileViewModel.onEvent(ProfileUiEvents.ChangedPhoneNumber(it.trim())) },
                                label = "Phone name",
                                errorMessage = uiState.phoneNameErr,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardType = KeyboardType.Phone
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CustomUpdateButton(
                                onClick = {
                                    if ( uiState.phoneNameErr == null && uiState.newPhoneNumber.isNotEmpty() ) {
                                        profileViewModel.onEvent(ProfileUiEvents.UpdateButtonClicked(context.contentResolver))
                                    } else {
                                        Toast.makeText(context, "Invalid form!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                text = "Update phone number",
                                enabled = uiState.isUpdating.not() && uiState.signingOut.not() ,
                                updating = uiState.isUpdating
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        null -> Unit
                    }
                    Button(
                        onClick = { showBottomSheet = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Cancel", color = Color.Black)
                    }
                }
            }
        }

        if( showConfirmLogoutDialog ) {
            CustomAlertDialog(
                title = "Log out?",
                text1 = "You are about to logout! Would you like to proceed?",
                confirmButtonText = "Yes",
                onConfirm = {
                    showConfirmLogoutDialog = false
                    profileViewModel.onEvent(ProfileUiEvents.LoggedOut)
                },
                onDismiss = {
                    showConfirmLogoutDialog = false
                }
            )
        }
    }
}
