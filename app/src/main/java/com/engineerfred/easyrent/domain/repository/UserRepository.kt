package com.engineerfred.easyrent.domain.repository

import android.content.ContentResolver
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUerInfo() : Flow<Resource<User?>>
    suspend fun updateUserNames(firstName: String, lastName: String) : Resource<Unit>
    suspend fun updateHostelName(name: String) : Resource<Unit>
    suspend fun updateTelephoneNumber(telNo: String) : Resource<Unit>
    suspend fun updateProfilePicture(profilePicUrl: String, contentResolver: ContentResolver) : Resource<Unit>
}