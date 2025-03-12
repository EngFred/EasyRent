package com.engineerfred.easyrent.domain.usecases.user

import com.engineerfred.easyrent.domain.repository.UserRepository
import javax.inject.Inject

class FetchUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() = userRepository.getUerInfo()
}