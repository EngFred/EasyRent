package com.engineerfred.easyrent.presentation.screens.profile

import android.annotation.SuppressLint
import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.UserInfoUpdateStatus
import com.engineerfred.easyrent.domain.usecases.auth.SignOutUserUseCase
import com.engineerfred.easyrent.domain.usecases.payments.GetAllPaymentsUseCase
import com.engineerfred.easyrent.domain.usecases.rooms.GetRoomsUseCase
import com.engineerfred.easyrent.domain.usecases.user.FetchUserInfoUseCase
import com.engineerfred.easyrent.domain.usecases.user.UpdateHostelNameUseCase
import com.engineerfred.easyrent.domain.usecases.user.UpdatePhoneNumberUseCase
import com.engineerfred.easyrent.domain.usecases.user.UpdateProfileImageUseCase
import com.engineerfred.easyrent.domain.usecases.user.UpdateUserNamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val fetchUserInfoUseCase: FetchUserInfoUseCase,
    private val signOutUserUseCase: SignOutUserUseCase,
    private val updateUserNamesUseCase: UpdateUserNamesUseCase,
    private val updateHostelNameUseCase: UpdateHostelNameUseCase,
    private val updatePhoneNumberUseCase: UpdatePhoneNumberUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val getAllPaymentsUseCase: GetAllPaymentsUseCase,
    private val getAllRoomsUseCase: GetRoomsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchUserInfo()
        fetchRooms()
        fetchPayments()
    }

    fun onEvent(event: ProfileUiEvents) {
        if ( _uiState.value.signOutErr != null ) {
            _uiState.update {
                it.copy(
                    signOutErr = null
                )
            }
        }
        when(event) {
            ProfileUiEvents.RetryClicked -> {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        user = null
                    )
                }
                fetchUserInfo()
            }
            ProfileUiEvents.LoggedOut -> {
                signOut()
            }

            is ProfileUiEvents.ChangedFirstName -> {
                verifyFirstName(event.newFirstName)
                _uiState.update {
                    it.copy(
                        newFirstName = event.newFirstName.trim()
                    )
                }
            }
            is ProfileUiEvents.ChangedHostelName -> {
                verifyHostelName(event.newHostelName)
                _uiState.update {
                    it.copy(
                        newHostelName = event.newHostelName.trim()
                    )
                }
            }
            is ProfileUiEvents.ChangedLastName -> {
                verifyLastName(event.newLastName)
                _uiState.update {
                    it.copy(
                        newLastName = event.newLastName.trim()
                    )
                }
            }
            is ProfileUiEvents.ChangedPhoneNumber -> {
                verifyPhoneNumber(event.newPhoneNumber)
                _uiState.update {
                    it.copy(
                        newPhoneNumber = event.newPhoneNumber.trim()
                    )
                }
            }
            is ProfileUiEvents.SelectedNewProfileImage -> {
                _uiState.update {
                    it.copy(
                        selectedImgUrl = event.imageUrl
                    )
                }
            }

            is ProfileUiEvents.ChangedUpdateState -> {
                refreshState()
                _uiState.update {
                    it.copy(
                        updateState = event.userInfoUpdateStatus
                    )
                }
                when(event.userInfoUpdateStatus) {
                    UserInfoUpdateStatus.UpdatingProfileImage -> {
                        _uiState.update {
                            it.copy(
                                selectedImgUrl = _uiState.value.user?.imageUrl
                            )
                        }
                    }
                    UserInfoUpdateStatus.UpdatingNames -> {
                        _uiState.update {
                            it.copy(
                                newFirstName = _uiState.value.user?.firstName ?: "",
                                newLastName = _uiState.value.user?.lastName ?: ""
                            )
                        }
                    }

                    UserInfoUpdateStatus.UpdatingHostelName -> {
                        _uiState.update {
                            it.copy(
                                newHostelName = _uiState.value.user?.hostelName ?: ""
                            )
                        }
                    }

                    UserInfoUpdateStatus.UpdatingPhoneNumber -> {
                        _uiState.update {
                            it.copy(
                                newPhoneNumber = _uiState.value.user?.telNo ?: ""
                            )
                        }
                    }
                }
            }

            is ProfileUiEvents.UpdateButtonClicked -> {
                val updateSate = _uiState.value.updateState
                val uiSateValues = _uiState.value

                _uiState.update {
                    it.copy(isUpdating = true)
                }

                if ( updateSate != null ) {
                    when(updateSate) {
                        UserInfoUpdateStatus.UpdatingNames -> {
                            if ( uiSateValues.newFirstName != uiSateValues.user?.firstName || uiSateValues.newLastName != uiSateValues.user.lastName ) {
                                updateUserNames(uiSateValues.newFirstName, uiSateValues.newLastName)
                            } else {
                                _uiState.update {
                                    it.copy(isUpdating = false)
                                }
                            }
                        }
                        UserInfoUpdateStatus.UpdatingPhoneNumber -> {
                            if ( uiSateValues.newPhoneNumber != uiSateValues.user?.telNo ) {
                                updateUserPhoneNumber(uiSateValues.newPhoneNumber)
                            } else {
                                _uiState.update {
                                    it.copy(isUpdating = false)
                                }
                            }
                        }
                        UserInfoUpdateStatus.UpdatingHostelName -> {
                            if ( uiSateValues.newHostelName != uiSateValues.user?.hostelName ) {
                                updateUserHostelName(uiSateValues.newHostelName)
                            } else {
                                _uiState.update {
                                    it.copy(isUpdating = false)
                                }
                            }
                        }
                        UserInfoUpdateStatus.UpdatingProfileImage -> {
                            uiSateValues.selectedImgUrl?.let {
                                updateUserProfileImage(uiSateValues.selectedImgUrl, event.contentResolver)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun refreshState() {
        _uiState.update {
            it.copy(
                updateState = null,
                firstNameErr = null,
                newFirstName = "",
                lastNameErr = null,
                newLastName = "",
                hostelNameErr = null,
                newHostelName = "",
                phoneNameErr = null,
                newPhoneNumber = "",
                selectedImgUrl = null
            )
        }
    }

    private fun signOut() = viewModelScope.launch( Dispatchers.IO ) {
        _uiState.update {
            it.copy(
                signingOut = true,
                signOutErr = null
            )
        }
        val result = signOutUserUseCase.invoke()
        when(result) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        signingOut = false,
                        signOutErr = result.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        signingOutSuccess = true
                    )
                }
            }
        }
    }

    private fun verifyLastName(lastName: String) {
        val namePattern = "^[A-Za-z]{2,50}$".toRegex()
        _uiState.update {
            it.copy(
                lastNameErr = when {
                    lastName.isBlank() -> "Last Name cannot be empty"
                    !lastName.matches(namePattern) -> "Invalid name!"
                    else -> null
                }
            )
        }
    }

    private fun verifyFirstName(firstName: String) {
        val namePattern = "^[A-Za-z]{2,50}$".toRegex()
        _uiState.update {
            it.copy(
                firstNameErr = when {
                    firstName.isBlank() -> "First Name cannot be empty"
                    !firstName.matches(namePattern) -> "Invalid name!"
                    else -> null
                }
            )
        }
    }

    private fun verifyHostelName(hostelName: String) {
        val namePattern = "^[A-Za-z]{2,50}$".toRegex()
        _uiState.update {
            it.copy(
                hostelNameErr = when {
                    hostelName.isBlank() -> "First Name cannot be empty"
                    !hostelName.matches(namePattern) -> "Invalid name!"
                    else -> null
                }
            )
        }
    }

    private fun verifyPhoneNumber(phoneNumber: String) {
        val phonePattern = "^\\+?[1-9][0-9]{7,14}$".toRegex()  // Supports international formats
        _uiState.update {
            it.copy(
                phoneNameErr = when {
                    phoneNumber.isBlank() -> "Phone number cannot be empty"
                    !phoneNumber.matches(phonePattern) -> "Invalid phone number format"
                    else -> null
                }
            )
        }
    }

    private fun fetchUserInfo() = viewModelScope.launch( Dispatchers.IO ) {
        fetchUserInfoUseCase.invoke().collect{ result ->
            when(result) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.msg
                        )
                    }
                }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data
                        )
                    }
                }
            }
        }
    }


    private fun updateUserNames(firstName: String, lastName: String)  = viewModelScope.launch(Dispatchers.IO) {
        updateUserNamesUseCase(firstName, lastName)
        _uiState.update {
            it.copy( isUpdating = false )
        }
    }

    private fun updateUserPhoneNumber(phoneNumber: String) = viewModelScope.launch( Dispatchers.IO ) {
        updatePhoneNumberUseCase(phoneNumber)
        _uiState.update {
            it.copy( isUpdating = false )
        }
    }

    private fun updateUserHostelName(hostelName: String)  = viewModelScope.launch( Dispatchers.IO ) {
        updateHostelNameUseCase(hostelName)
        _uiState.update {
            it.copy( isUpdating = false )
        }
    }

    private fun updateUserProfileImage( imageUrl: String, contentResolver: ContentResolver ) = viewModelScope.launch( Dispatchers.IO ) {
        updateProfileImageUseCase(imageUrl, contentResolver)
        _uiState.update {
            it.copy( isUpdating = false )
        }
    }

    private fun fetchRooms() = viewModelScope.launch {
        getAllRoomsUseCase().collect{ result ->
            when(result){
                is Resource.Success -> {
                    _uiState.update {
                        it.copy( rooms = result.data )
                    }
                }
                else -> Unit
            }
        }
    }

    @SuppressLint("NewApi")
    private fun fetchPayments() = viewModelScope.launch {
        getAllPaymentsUseCase.invoke().collect{ result ->
            when(result){
                is Resource.Success -> {
                    val payments = result.data.filter {
                        val currentMonth = LocalDate.now().monthValue
                        val currentYear = LocalDate.now().year
                        val paymentDate = Instant.ofEpochMilli(it.paymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
                        val paymentMonth = paymentDate.monthValue
                        val paymentYear = paymentDate.year
                        paymentMonth == currentMonth && paymentYear == currentYear
                    }

                    _uiState.update {
                        it.copy( payments = payments )
                    }
                }
                else -> Unit
            }
        }
    }

}