package com.engineerfred.easyrent.presentation.screens.signup

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.domain.usecases.auth.SignUpUserUseCase
import com.engineerfred.easyrent.util.verifyEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUserUseCase: SignUpUserUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: SignUpEvents) {
        if ( _uiState.value.signUpErr != null ) {
            _uiState.update {
                it.copy(
                    signUpErr = null
                )
            }
        }
        when(event){
            is SignUpEvents.EmailChanged -> {
                //verify email
                val emailVerificationResults = verifyEmail(event.email)
                _uiState.update { 
                    it.copy(
                       email = event.email,
                        emailErr = emailVerificationResults
                    )
                }
            }
            is SignUpEvents.FirstNameChanged -> {
                //verify name
                verifyFirstName(event.firstName)
                _uiState.update {
                    it.copy(
                        firstName = event.firstName.trim()
                    )
                }
            }
            is SignUpEvents.ImageUrlChangedChanged -> {
                _uiState.update {
                    it.copy(
                        imageUrl = event.imageUrl
                    )
                }
            }
            is SignUpEvents.LastNameChanged -> {
                //verify name
                verifyLastName(event.lastName)
                _uiState.update {
                    it.copy(
                        lastName = event.lastName.trim()
                    )
                }
            }
            is SignUpEvents.PasswordChanged -> {
                //verify name
                verifyPassword(event.password)
                _uiState.update {
                    it.copy(
                        password = event.password.trim()
                    )
                }
            }
            is SignUpEvents.SignUpButtonClicked -> {
                val stateValue = _uiState.value
                if ( stateValue.firstNameErr == null &&
                    stateValue.lastNameErr == null &&
                    stateValue.emailErr == null &&
                    stateValue.passwordErr == null &&
                    stateValue.telNoErr == null &&
                    stateValue.ck
                ){

                    val user = User(
                        id = "",
                        firstName = stateValue.firstName.trim(),
                        lastName = stateValue.lastName.trim(),
                        imageUrl = stateValue.imageUrl,
                        email = stateValue.email,
                        telNo = stateValue.telNo,
                        hostelName = stateValue.hostelName?.trim()
                    )
                    signUp(user, stateValue.password, event.contentResolver)
                }
            }
            is SignUpEvents.TelNoChanged -> {
                //verify name
                verifyPhoneNumber(event.telNo)
                _uiState.update {
                    it.copy(
                       telNo = event.telNo.trim()
                    )
                }
            }

            is SignUpEvents.HostelNameChanged -> {
                verifyHostelName(event.hostelName)
                _uiState.update {
                    it.copy(
                        hostelName = event.hostelName.trim()
                    )
                }
            }
        }
    }

    private fun signUp(user: User, password: String, contentResolver: ContentResolver) = viewModelScope.launch( Dispatchers.IO ) {
        _uiState.update {
            it.copy(
                signingUp = true
            )
        }
        val result = signUpUserUseCase.invoke(user, password, contentResolver)
        when(result) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        signingUp = false,
                        signUpErr = result.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        signUpSuccessful = true
                    )
                }
            }
        }
    }

    private fun verifyPhoneNumber(telNo: String) {
        val phonePattern = "^\\+?[1-9][0-9]{7,14}$".toRegex()  // Supports international formats
        _uiState.update {
            it.copy(
                telNoErr = when {
                    telNo.isBlank() -> "Phone number cannot be empty"
                    !telNo.matches(phonePattern) -> "Invalid phone number format"
                    else -> null
                }
            )
        }
    }

    private fun verifyPassword(password: String) {
        _uiState.update {
            it.copy(
                passwordErr = when {
                    password.length < 8 -> "Password must be at least 8 characters long"
                    !password.any { it.isDigit() } -> "Password must contain at least one digit"
                    !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
                    !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
                    !password.any { "!@#$%^&*()_-+=<>?/{}~".contains(it) } -> "Password must contain at least one special character"
                    else -> null
                }
            )
        }
    }

    private fun verifyFirstName(firstName: String) {
        val namePattern = "^[A-Za-z]{2,50}$".toRegex()  // Allows letters and no spaces, min 2 chars
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
        val namePattern = "^[A-Za-z]{2,50}$".toRegex()  // Allows letters and no spaces, min 2 chars
        _uiState.update {
            it.copy(
                hostelNameErr = when {
                    hostelName.isBlank() -> null
                    !hostelName.matches(namePattern) -> "Invalid name!"
                    else -> null
                }
            )
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

}