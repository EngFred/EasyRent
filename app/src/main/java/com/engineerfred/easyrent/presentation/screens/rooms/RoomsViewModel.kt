package com.engineerfred.easyrent.presentation.screens.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.domain.usecases.rooms.DeleteRoomUseCase
import com.engineerfred.easyrent.domain.usecases.rooms.GetRoomsUseCase
import com.engineerfred.easyrent.domain.usecases.tenants.GetTenantInRoomUseCase
import com.engineerfred.easyrent.domain.usecases.tenants.RemoveTenantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomsViewModel @Inject constructor(
    private val getRoomsUseCase: GetRoomsUseCase,
    private val getTenantInRoomUseCase: GetTenantInRoomUseCase,
    private val removeTenantUseCase: RemoveTenantUseCase,
    private val deleteRoomUseCase: DeleteRoomUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomsState())
    val uiState = _uiState.asStateFlow()

    init {
        getRooms()
    }

    fun onEvent(event: RoomsEvents) {
        when(event) {
            RoomsEvents.ClickedRetryButton -> {}
            is RoomsEvents.RoomSelected -> {
                _uiState.update {
                    it.copy(
                        roomId = event.roomId
                    )
                }
                getTenantInRoom(event.roomId)
            }

            is RoomsEvents.TenantDeleted -> {
                deleteTenant(event.tenant)
            }

            is RoomsEvents.RoomDeleted -> {
                deleteRoom(event.room)
            }
        }
    }

    private fun deleteTenant(tenant: Tenant) = viewModelScope.launch( Dispatchers.IO ) {
        _uiState.update {
            it.copy(
                isDeletingTenant = true,
                deletingTenantErr = null,
                deleteTenantSuccessful = false
            )
        }
        val result = removeTenantUseCase.invoke(tenant)
        when(result) {
            is Resource.Error ->  {
                _uiState.update {
                    it.copy(
                        isDeletingTenant = false,
                        deletingTenantErr = result.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        isDeletingTenant = false,
                        deleteTenantSuccessful = true,
                        deletingTenantErr = null
                    )
                }
            }
        }
    }

    private fun getRooms() = viewModelScope.launch(Dispatchers.IO) {
        getRoomsUseCase().collect{
            when(it) {
                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            loading = false,
                            error = state.error
                        )
                    }
                }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            loading = false,
                            rooms = it.data
                        )
                    }
                }
            }
        }
    }

    private fun getTenantInRoom(roomId: String)  = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update {
            it.copy(
                fetchingRoomTenant = true
            )
        }
        val result = getTenantInRoomUseCase.invoke(roomId)
        when(result) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        fetchingRoomTenant = false
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        fetchingRoomTenant = false,
                        roomId = result.data?.roomId,
                        tenant = result.data
                    )
                }
            }
        }
    }


    private fun deleteRoom(room: Room) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update {
            it.copy(
                deletingRoom = true,
                deletingRoomErr = null
            )
        }
        val result = deleteRoomUseCase.invoke(room)
        when(result) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        deletingRoom = false,
                        deletingRoomErr = result.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        deletingRoom = false
                    )
                }
            }
        }
    }

}