package com.engineerfred.easyrent.presentation.screens.tenants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.domain.usecases.tenants.GetAllTenantsUseCase
import com.engineerfred.easyrent.domain.usecases.tenants.GetUnsyncedTenantsUseCase
import com.engineerfred.easyrent.domain.usecases.tenants.RemoveTenantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantsViewModel @Inject constructor(
    private val getAllTenantsUseCase: GetAllTenantsUseCase,
    private val removeTenantUseCase: RemoveTenantUseCase,
    private val getUnsyncedTenantsUseCase: GetUnsyncedTenantsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantsUiState())
    val uiState = _uiState.asStateFlow()

    private val _deletingTenantId = MutableStateFlow("")
    val deletingTenantId = _deletingTenantId.asStateFlow()

    init {
        fetchTenants()
        getUnsyncedTenants()
    }

    private fun fetchTenants() = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true
            )
        }
        getAllTenantsUseCase.invoke().collect{ result ->
            when(result) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fetchError = result.msg
                        )
                    }
                }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tenants = result.data
                        )
                    }
                }
            }
        }
    }


    fun deleteTenant(tenant: Tenant) = viewModelScope.launch( Dispatchers.IO ) {
        _uiState.update {
            it.copy(
                deletingTenant = true,
                deletingTenantErr = null,
                deleteSuccessful = false
            )
        }
        _deletingTenantId.value = tenant.id
        val result = removeTenantUseCase.invoke(tenant)
        when(result) {
            is Resource.Error ->  {
                _uiState.update {
                    it.copy(
                        deletingTenant = false,
                        deletingTenantErr = result.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        deletingTenant = false,
                        deleteSuccessful = true,
                        deletingTenantErr = null
                    )
                }
            }
        }
    }

    private fun getUnsyncedTenants() = viewModelScope.launch( Dispatchers.IO ) {
        val result = getUnsyncedTenantsUseCase.invoke()

        result?.let {

            _uiState.update {
                it.copy(
                    unSyncedTenants = result
                )
            }

            if ( result.isNotEmpty() ) {
                delay(2000)
                _uiState.update {
                    it.copy(
                        showSyncRequired = true,
                        showSyncButton = true
                    )
                }
                delay(5000)
                _uiState.update {
                    it.copy(
                        showSyncRequired = false
                    )
                }
                delay(5000)
                _uiState.update {
                    it.copy(
                        showSyncButton = false
                    )
                }
            }
        }
    }

    fun hideSyncButton(){
        _uiState.update {
            it.copy(
                showSyncButton = false,
                showSyncRequired = false
            )
        }
    }

}