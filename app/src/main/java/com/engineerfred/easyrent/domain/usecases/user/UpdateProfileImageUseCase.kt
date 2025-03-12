package com.engineerfred.easyrent.domain.usecases.user

import android.content.ContentResolver
import com.engineerfred.easyrent.domain.repository.UserRepository
import javax.inject.Inject

class UpdateProfileImageUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(imageUrl: String, contentResolver: ContentResolver) = userRepository.updateProfilePicture(imageUrl, contentResolver)
}