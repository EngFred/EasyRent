package com.engineerfred.easyrent.domain.usecases.auth

import android.content.ContentResolver
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(user: User, password: String, contentResolver: ContentResolver) = authRepository.signUpUser(user, password, contentResolver)
}