package com.engineerfred.easyrent.domain.repository

import android.content.ContentResolver
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.User

interface AuthRepository {
    suspend fun signUpUser(user: User, password: String, contentResolver: ContentResolver) : Resource<Unit>
    suspend fun signInUser(email: String, password: String) : Resource<Unit>
    suspend fun signOut() : Resource<Unit>
}