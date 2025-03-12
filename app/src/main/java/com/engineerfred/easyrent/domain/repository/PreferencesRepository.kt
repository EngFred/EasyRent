package com.engineerfred.easyrent.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    suspend fun saveUserId(userId: String)
    suspend fun clearUserId()
    fun getUserId() : Flow<String?>
}