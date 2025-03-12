package com.engineerfred.easyrent.domain.usecases.user

import com.engineerfred.easyrent.domain.repository.UserRepository
import javax.inject.Inject

class UpdatePhoneNumberUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(phoneNumber: String) = userRepository.updateTelephoneNumber(phoneNumber)
}