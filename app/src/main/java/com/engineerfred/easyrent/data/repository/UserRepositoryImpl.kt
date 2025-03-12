package com.engineerfred.easyrent.data.repository

import android.content.ContentResolver
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.core.net.toUri
import com.engineerfred.easyrent.constants.Constants.SUPABASE_URL
import com.engineerfred.easyrent.constants.Constants.USERS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toUser
import com.engineerfred.easyrent.data.mappers.toUserInfoEntity
import com.engineerfred.easyrent.data.remote.dto.UserDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.domain.repository.UserRepository
import com.engineerfred.easyrent.util.compressAndConvertToByteArray
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    private val currentUser = supabaseClient.auth.currentUserOrNull()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)

    override suspend fun getUerInfo(): Flow<Resource<User?> > = flow {
        Log.i(TAG, "Fetching user info from cache...")
        val userIdInPrefs = prefs.getUserId().firstOrNull()
        val userId = if ( userIdInPrefs == currentUser!!.id ) currentUser.id else userIdInPrefs
        val userFlow = cache.userInfoDao().getUserInfo( userId ?: currentUser.id ).map{ cachedUserInfo ->
            if ( cachedUserInfo != null ) {
                Log.i(TAG, "Successfully fetched user from cache!")
                Resource.Success(cachedUserInfo.toUser())
            } else {
                Log.i(TAG, "User not in cache! Fetching user from supabase...")
                val user = supabaseClient.from(USERS).select {
                    filter { eq("id", userId ?: currentUser.id) }
                }.decodeSingleOrNull<UserDto>()

                if (user != null) {
                    Log.i(TAG, "Successfully fetched user from supabase! CACHING USER....")
                    cache.userInfoDao().insertUserInfo(user.toUserInfoEntity())
                    Log.i(TAG, "Successfully cached user from supabase!")
                    Resource.Success(user.toUser())
                } else {
                    Log.i(TAG, "User not found anywhere!")
                    Resource.Error("User not found anywhere!")
                }
            }
        }
        emitAll(userFlow)
    }.flowOn(Dispatchers.IO).catch {
        Log.i(TAG, "${it.message}")
        emit(Resource.Error("$it"))
    }

    override suspend fun updateUserNames(firstName: String, lastName: String): Resource<Unit> {
        return try {
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            if ( !userIdInPrefs.isNullOrEmpty() && userIdInPrefs == currentUser?.id) {
                Log.d(TAG, "Updating usernames in supabase...")
                supabaseClient.from(USERS).update(
                    {
                        set("first_name", firstName)
                        set("last_name", lastName)
                    }
                ){
                    filter {
                        eq("id", userIdInPrefs)
                    }
                }
                Log.d(TAG, "Updating usernames in cache...")
                cache.userInfoDao().updateUserNames(userIdInPrefs, firstName, lastName)
                Log.i(TAG, "Usernames updated successfully!")
                Resource.Success(Unit)
            } else {
                Resource.Error("User is not authenticated!")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating user names: ${ex.message}")
            Resource.Error("${ex.message}")
        }
    }

    override suspend fun updateHostelName(name: String): Resource<Unit> {
        return try {
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            if ( !userIdInPrefs.isNullOrEmpty() && userIdInPrefs == currentUser?.id) {
                Log.d(TAG, "Updating hostel name in supabase...")
                supabaseClient.from(USERS).update(
                    {
                        set("hostel_name", name)
                    }
                ){
                    filter {
                        eq("id", userIdInPrefs)
                    }
                }
                Log.d(TAG, "Updating hostel name in cache...")
                cache.userInfoDao().updateHostelName(userIdInPrefs, name)
                Log.i(TAG, "Hostel name updated successfully!")
                Resource.Success(Unit)
            } else {
                Resource.Error("BAD REQUEST!")
            }
        }  catch (ex: Exception) {
            Log.e(TAG, "Error updating hostel name: ${ex.message}")
            Resource.Error("${ex.message}")
        }
    }

    override suspend fun updateTelephoneNumber(telNo: String): Resource<Unit> {
        return try {
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            if ( !userIdInPrefs.isNullOrEmpty() && userIdInPrefs == currentUser?.id) {
                Log.d(TAG, "Updating user telephone number in supabase...")
                supabaseClient.from(USERS).update(
                    {
                        set("tel_no", telNo)
                    }
                ){
                    filter {
                        eq("id", userIdInPrefs)
                    }
                }
                Log.d(TAG, "Updating user telephone number in cache...")
                cache.userInfoDao().updateTelephoneNumber(userIdInPrefs, telNo)
                Log.d(TAG, "User telephone number updated successfully!")
                Resource.Success(Unit)
            } else {
                Resource.Error("BAD REQUEST!")
            }
        }  catch (ex: Exception) {
            Log.e(TAG, "Error updating telephone number: ${ex.message}")
            Resource.Error("${ex.message}")
        }
    }


    override suspend fun updateProfilePicture(profilePicUrl: String, contentResolver: ContentResolver): Resource<Unit> {
        return try {
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            if ( !userIdInPrefs.isNullOrEmpty() && userIdInPrefs == currentUser?.id) {
                if(profilePicUrl.isNotEmpty()) {
                    Log.i(TAG, "Updating profile image (supabase)...")
                    withContext(NonCancellable) {
                        val imageUri = profilePicUrl.toUri()
                        val fileName = "${userIdInPrefs}_${System.currentTimeMillis()}_IMG.png"

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
                                set("image_url", buildImageUrl(uploadedImgUrl.path))
                            }
                        ) {
                            filter {
                                eq("id", userIdInPrefs)
                            }
                        }

                        Log.i(TAG, "Profile image updated successfully in supabase! Updating in cache...")
                        cache.userInfoDao().updateUserProfileImage(userIdInPrefs, buildImageUrl(uploadedImgUrl.path))
                        Log.i(TAG, "Profile image updated successfully! Image path: ${buildImageUrl(uploadedImgUrl.path)}")
                        Resource.Success(Unit)
                    }
                } else {
                    Log.i(TAG, "Profile image is empty!")
                    Resource.Error("Profile image not found!")
                }
            } else {
                Log.i(TAG, "User is not authenticated!")
                Resource.Error("BAD REQUEST!")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating profile picture: ${ex.message}")
            Resource.Error("${ex.message}")
        }
    }

    private fun buildImageUrl(imageFileName: String) =
        "$SUPABASE_URL/storage/v1/object/users-images/${imageFileName}"
}