package com.engineerfred.easyrent.presentation.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.usecases.auth.SignInUserUseCase
import com.engineerfred.easyrent.util.verifyEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUserUseCase: SignInUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUIState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: SignInUIEvents) {
        if ( _uiState.value.signInError != null ) {
            _uiState.update {
                it.copy(
                    signInError = null
                )
            }
        }
        when(event) {
            is SignInUIEvents.EmailChanged -> {
                val emailVerificationResults = verifyEmail(event.email)
                _uiState.update {
                    it.copy(
                        email = event.email.trim(),
                        emailError = emailVerificationResults
                    )
                }
            }
            is SignInUIEvents.PasswordChanged -> {
                _uiState.update {
                    it.copy(
                        password = event.password.trim()
                    )
                }
            }
            SignInUIEvents.SignInButtonClicked -> {
                val stateValues = _uiState.value
                if( stateValues.emailError.isNullOrEmpty() && stateValues.password.isNotEmpty() ) {
                    signIn(stateValues.email, stateValues.password)
                }
            }
        }
    }

    private fun signIn(email: String, password: String) = viewModelScope.launch( Dispatchers.IO ) {
        _uiState.update {
            it.copy(
                signingIn = true
            )
        }
        val results = signInUserUseCase.invoke(email, password)
        when(results) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        signingIn = false,
                        signInError = results.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        signInSuccessful = true
                    )
                }
            }
        }
    }
}