package com.engineerfred.easyrent.presentation.screens.add_room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.usecases.rooms.InsertRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRoomViewModel @Inject constructor(
    private val insertRoomUseCase: InsertRoomUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRoomUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: AddRoomUiEvents){
        if ( _uiState.value.insertionErr != null ) {
            _uiState.update {
                it.copy(
                    insertionErr = null
                )
            }
        }
        when(event) {
            is AddRoomUiEvents.SelectedRoomType -> {
                _uiState.update {
                    it.copy(
                        selectedRoomType = event.roomType
                    )
                }
            }

            is AddRoomUiEvents.ChangedMonthlyPayment -> {
                _uiState.update {
                    it.copy(
                        monthlyPayment = event.monthlyPayment,
                        monthlyPaymentErrMessage = if (event.monthlyPayment.isEmpty()) "Add Monthly payment" else if (event.monthlyPayment.toFloatOrNull() == null) "Invalid amount!" else null
                    )
                }
            }

            is AddRoomUiEvents.CheckedOccupied -> {
                _uiState.update {
                    it.copy(
                        isOccupied = event.isOccupied
                    )
                }
            }

            AddRoomUiEvents.SaveClicked -> {
                _uiState.update {
                    it.copy(
                        inserting = true
                    )
                }
                val room = Room(
                    roomType = _uiState.value.selectedRoomType.name.lowercase(),
                    monthlyRent = _uiState.value.monthlyPayment!!.toFloat(),
                    roomNumber = _uiState.value.roomNumber.toInt(),
                    isOccupied = false,
                    userId = "" //it won't be empty inside the repository
                )
                saveRoom(room)
            }

            is AddRoomUiEvents.ChangedRoomNumber -> {
                _uiState.update { state ->
                    state.copy(
                        roomNumber = event.roomNumber,
                        roomNumberErrMessage = if (event.roomNumber.isEmpty()) "Add Room number" else if (event.roomNumber.toIntOrNull() == null) "Invalid number!" else null
                    )
                }
            }
        }
    }

    private fun saveRoom(room: Room) = viewModelScope.launch( Dispatchers.IO ) {
        val task = insertRoomUseCase.invoke(room)
        when(task){
            is Resource.Error -> {
                _uiState.update {  state ->
                    state.copy(
                        insertionErr = task.msg,
                        inserting = false
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {  state ->
                    state.copy(
                        createdRoomId = task.data
                    )
                }
            }
        }
    }
}