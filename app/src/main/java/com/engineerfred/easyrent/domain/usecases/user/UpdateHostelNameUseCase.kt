package com.engineerfred.easyrent.domain.usecases.user

import com.engineerfred.easyrent.domain.repository.UserRepository
import javax.inject.Inject

class UpdateHostelNameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(hostelName: String) = userRepository.updateHostelName(hostelName)
}