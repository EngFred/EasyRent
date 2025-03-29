package com.engineerfred.easyrent.presentation.screens.tenants

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkManager
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.presentation.common.CustomSyncToast
import com.engineerfred.easyrent.presentation.screens.tenants.components.TenantItem
import com.engineerfred.easyrent.presentation.theme.MyError
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary
import com.engineerfred.easyrent.util.WorkerUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tenants(
    onAddTenant: () -> Unit,
    onBackClicked: () -> Unit,
    onImageClicked: (String) -> Unit,
    tenantsViewModel: TenantsViewModel = hiltViewModel(),
    workManager: WorkManager,
    user: User?
) {

    val uiState = tenantsViewModel.uiState.collectAsState().value
    val deletingTenantId = tenantsViewModel.deletingTenantId.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.deletingTenantErr) {
        if ( uiState.deletingTenantErr != null ) {
            Toast.makeText(context, uiState.deletingTenantErr, Toast.LENGTH_LONG).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tenantsViewModel.hideSyncButton()
        }
    }

    Scaffold(

        topBar = {
            TopAppBar(title = { Text(text = "Tenants") },
                navigationIcon = {
                    IconButton(onClick = { onBackClicked() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MySecondary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            onAddTenant()
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Person, contentDescription = null)
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        }
                    }
                    Spacer(Modifier.size(16.dp))
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible= uiState.showSyncButton) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 45.dp, end = 20.dp),
                    onClick = {
                        WorkerUtils.syncTenantsImmediately(workManager)
                        tenantsViewModel.hideSyncButton()
                    },
                    containerColor = MyPrimary
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                }

                !uiState.isLoading && !uiState.fetchError.isNullOrEmpty() -> {
                    Text(
                        text = uiState.fetchError,
                        style = TextStyle(
                            fontWeight = FontWeight.W200,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            color = MyError,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = .5f),
                                blurRadius = 6f,
                                offset = Offset(2f, 2f)
                            )
                        )
                    )
                }

                else -> {
                    if (uiState.tenants.isEmpty()) {
                        Text(
                            text = "No tenants found!",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.W200,
                                textAlign = TextAlign.Center,
                                color = MySurface,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = .5f),
                                    blurRadius = 6f,
                                    offset = Offset(2f, 2f)
                                )
                            )
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                items(count = uiState.tenants.size, key = { uiState.tenants[it].id }) {
                                    val currentTenant = uiState.tenants[it]
                                    TenantItem(
                                        tenant = currentTenant,
                                        onDelete = {
                                            tenantsViewModel.deleteTenant(currentTenant)
                                        },
                                        deletedTenantId = deletingTenantId,
                                        deletingTenant = { uiState.deletingTenant },
                                        onImageClicked = onImageClicked,
                                        user = user
                                    )
                                }
                            }
                            CustomSyncToast(
                                showSyncRequired = uiState.showSyncRequired,
                                dataCount = uiState.unSyncedTenants.size,
                                dataName = "tenant"
                            )
                        }
                    }
                }
            }
        }
    }
}