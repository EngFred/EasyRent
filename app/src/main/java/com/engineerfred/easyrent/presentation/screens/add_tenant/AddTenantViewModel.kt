package com.engineerfred.easyrent.presentation.screens.add_tenant

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.domain.usecases.rooms.GetAvailableRoomsUseCase
import com.engineerfred.easyrent.domain.usecases.tenants.AddTenantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTenantViewModel @Inject constructor(
    private val addTenantUseCase: AddTenantUseCase,
    private val getAvailableRoomsUseCase: GetAvailableRoomsUseCase
    //private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTenantUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: AddTenantUiEvents) {
        if ( _uiState.value.insertionErr != null ) {
            _uiState.update {
                it.copy(
                    insertionErr = null
                )
            }
        }
        when(event) {
            is AddTenantUiEvents.EmergencyContactChanged -> {
                _uiState.update {
                    it.copy(
                        emergencyContact = event.emergencyContact
                    )
                }
            }
            is AddTenantUiEvents.ChangedIdDetails -> {
                _uiState.update {
                    it.copy(
                        idDetails = event.idDetails
                    )
                }
            }
            is AddTenantUiEvents.SaveClicked -> {
                _uiState.update {
                    it.copy(
                        saving = true
                    )
                }
                val tenant = Tenant(
                    name = _uiState.value.name,
                    email = _uiState.value.email,
                    contact = _uiState.value.contact,
                    balance = _uiState.value.balance.toFloat(),
                    roomId = _uiState.value.selectedRoomId!!,
                    roomNumber = _uiState.value.roomNumber!!.toInt(),
                    emergencyContact = _uiState.value.emergencyContact,
                    idDetails = _uiState.value.idDetails,
                    notes = _uiState.value.notes,
                    profilePic = _uiState.value.imageUrl,
                    userId = ""
                )
                saveTenant(tenant, event.contentResolver)
            }
            is AddTenantUiEvents.ContactChanged -> {
                _uiState.update {
                    it.copy(
                        contact = event.contact
                    )
                }
            }
            is AddTenantUiEvents.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = event.name
                    )
                }
            }
            is AddTenantUiEvents.NotesChanged -> {
                _uiState.update {
                    it.copy(
                        notes = event.notes
                    )
                }
            }
            is AddTenantUiEvents.ImageUrlChanged -> {
                _uiState.update {
                    it.copy(
                        imageUrl = event.imageUrl
                    )
                }
            }

            is AddTenantUiEvents.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.email
                    )
                }
            }

            is AddTenantUiEvents.BalanceChanged -> {
                _uiState.update {
                    it.copy(
                        balance = event.balance
                    )
                }
            }

            is AddTenantUiEvents.SelectedRoomNumber -> {
                _uiState.update {
                    it.copy(
                        roomNumber = event.roomNumber
                    )
                }
            }

            is AddTenantUiEvents.SelectedRoomId -> {
                _uiState.update {
                    it.copy(
                        selectedRoomId = event.roomId
                    )
                }
            }

            AddTenantUiEvents.FetchedAvailableRooms -> {
                fetchAvailableRooms()
            }

            is AddTenantUiEvents.RoomSelected -> {
                _uiState.update {
                    it.copy(
                        selectedRoomId = event.room.id,
                        roomNumber = event.room.roomNumber.toString(),
                        balance = event.room.monthlyRent.toString()
                    )
                }
            }
        }
    }

    private fun saveTenant(tenant: Tenant, contentResolver: ContentResolver) = viewModelScope.launch(Dispatchers.IO) {
        val task = addTenantUseCase.invoke(tenant, contentResolver)
        when(task) {
            is Resource.Error -> {
                _uiState.update { state ->
                    state.copy(
                        insertionErr = task.msg,
                        saving = false
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        saveSuccess = true
                    )
                }
            }
        }
    }

    private fun fetchAvailableRooms() = viewModelScope.launch {
        getAvailableRoomsUseCase.invoke().collect{ task ->
            when(task) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            fetchingAvailableRooms = false
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update {
                        it.copy(
                            fetchingAvailableRooms = true
                        )
                    }
                }
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            fetchingAvailableRooms = false,
                            availableRooms = task.data
                        )
                    }
                }
            }
        }
    }
}