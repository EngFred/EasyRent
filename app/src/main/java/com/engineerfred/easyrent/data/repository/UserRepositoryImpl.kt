package com.engineerfred.easyrent.data.repository

import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import com.engineerfred.easyrent.constants.Constants.USERS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toUser
import com.engineerfred.easyrent.data.mappers.toUserInfoEntity
import com.engineerfred.easyrent.data.remote.dto.UserDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.domain.repository.UserRepository
import com.engineerfred.easyrent.util.buildImageUrl
import com.engineerfred.easyrent.util.compressAndConvertToByteArray
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val cache: CacheDatabase,
    private val prefs: PreferencesRepository
) : UserRepository {

    companion object {
        private val TAG =  UserRepositoryImpl::class.java.name
    }

    override suspend fun getUerInfo(): Flow<Resource<User?> > = flow {

        val userId = prefs.getUserId().firstOrNull() ?: run {
            emit(Resource.Error("User not logged in!"))
            return@flow
        }

        Log.i(TAG, "Fetching user info from cache...")

        val userFlow = cache.userInfoDao().getUserInfo( userId).map{ cachedUserInfo ->
            if ( cachedUserInfo != null ) {
                Log.i(TAG, "Successfully fetched user from cache!")
                Resource.Success(cachedUserInfo.toUser())
            } else {
                Log.i(TAG, "User not in cache! Fetching user from supabase...")
                val user = supabaseClient.from(USERS).select {
                    filter { eq("id", userId) }
                }.decodeSingleOrNull<UserDto>()

                if (user != null) {
                    Log.i(TAG, "Successfully fetched user from supabase! CACHING USER....")
                    cache.userInfoDao().insertUserInfo(user.toUserInfoEntity())

                    Log.i(TAG, "Successfully cached user from supabase!")
                    Resource.Success(user.toUser())
                } else {
                    Log.i(TAG, "User not found anywhere!")
                    Resource.Error("User not found!")
                }
            }
        }
        emitAll(userFlow)
    }.flowOn(Dispatchers.IO).catch {
        Log.i(TAG, "${it.message}")
        emit(Resource.Error("$it"))
    }.distinctUntilChanged()

    override suspend fun updateUserNames(firstName: String, lastName: String): Resource<Unit> {
        try {

            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            Log.d(TAG, "Updating usernames in supabase...")
            supabaseClient.from(USERS).update(
                {
                    set("first_name", firstName)
                    set("last_name", lastName)
                }
            ){
                filter {
                    eq("id", userId)
                }
            }
            Log.d(TAG, "Updating usernames in cache...")
            cache.userInfoDao().updateUserNames(userId, firstName, lastName)

            Log.i(TAG, "Usernames updated successfully!")
            return Resource.Success(Unit)
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating user names: ${ex.message}")
            return Resource.Error("${ex.message}")
        }
    }

    override suspend fun updateHostelName(name: String): Resource<Unit> {
        try {
            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            Log.d(TAG, "Updating hostel name in supabase...")
            supabaseClient.from(USERS).update(
                {
                    set("hostel_name", name)
                }
            ){
                filter {
                    eq("id", userId)
                }
            }
            Log.d(TAG, "Updating hostel name in cache...")
            cache.userInfoDao().updateHostelName(userId, name)

            Log.i(TAG, "Hostel name updated successfully!")
            return Resource.Success(Unit)

        }  catch (ex: Exception) {
            Log.e(TAG, "Error updating hostel name: ${ex.message}")
            return Resource.Error("${ex.message}")
        }
    }

    override suspend fun updateTelephoneNumber(telNo: String): Resource<Unit> {
        try {
            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            Log.d(TAG, "Updating user telephone number in supabase...")
            supabaseClient.from(USERS).update(
                {
                    set("tel_no", telNo)
                }
            ){
                filter {
                    eq("id", userId)
                }
            }

            Log.d(TAG, "Updating user telephone number in cache...")
            cache.userInfoDao().updateTelephoneNumber(userId, telNo)

            Log.d(TAG, "User telephone number updated successfully!")
            return Resource.Success(Unit)
        }  catch (ex: Exception) {
            Log.e(TAG, "Error updating telephone number: ${ex.message}")
            return Resource.Error("${ex.message}")
        }
    }


    override suspend fun updateProfilePicture(profilePicUrl: String, contentResolver: ContentResolver): Resource<Unit> {
        try {
            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            if(profilePicUrl.isNotEmpty()) {
                Log.i(TAG, "Updating profile image (supabase)...")
                withContext(NonCancellable) {
                    val imageUri = profilePicUrl.toUri()
                    val fileName = "${userId}_${System.currentTimeMillis()}_IMG.png"

                    // Convert and compress the image
                    val compressedImage = imageUri.compressAndConvertToByteArray(contentResolver)

                    val uploadedImgUrl = supabaseClient.storage.from("users-images").upload(
                        path = fileName,
                        data = compressedImage,
                    ) {
                        upsert = true
                        contentType = ContentType.Image.PNG
                    }
                    Log.i(TAG, "Profile image uploaded successfully in storage! Updating user image info in supabase...")

                    supabaseClient.from(USERS).update(
                        {
                            set("image_url", buildImageUrl(uploadedImgUrl.path, "users-images" ))
                        }
                    ) {
                        filter {
                            eq("id", userId)
                        }
                    }

                    Log.i(TAG, "Profile image updated successfully in supabase! Updating in cache...")
                    cache.userInfoDao().updateUserProfileImage(userId, buildImageUrl(uploadedImgUrl.path, "users-images"))

                    Log.i(TAG, "Profile image updated successfully! Image path: ${buildImageUrl(uploadedImgUrl.path, "users-images")}")
                    return@withContext Resource.Success(Unit)
                }
            }
            Log.i(TAG, "Profile image url is empty!")
            return Resource.Error("Profile image is empty!")
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating profile picture: ${ex.message}")
            return Resource.Error("${ex.message}")
        }
    }
}