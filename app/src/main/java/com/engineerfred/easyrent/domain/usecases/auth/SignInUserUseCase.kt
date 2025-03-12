package com.engineerfred.easyrent.domain.usecases.auth

import com.engineerfred.easyrent.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(email: String, password: String) = authRepository.signInUser(email, password)
}