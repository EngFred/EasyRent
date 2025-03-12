package com.engineerfred.easyrent.domain.usecases.auth

import com.engineerfred.easyrent.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke() = authRepository.signOut()
}