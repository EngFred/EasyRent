package com.engineerfred.easyrent.data.repository

import android.content.ContentResolver
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.core.net.toUri
import com.engineerfred.easyrent.constants.Constants.EXPENSES
import com.engineerfred.easyrent.constants.Constants.PAYMENTS
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.constants.Constants.SUPABASE_URL
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.constants.Constants.USERS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toExpenseEntity
import com.engineerfred.easyrent.data.mappers.toPaymentEntity
import com.engineerfred.easyrent.data.mappers.toRoomEntity
import com.engineerfred.easyrent.data.mappers.toTenantEntity
import com.engineerfred.easyrent.data.mappers.toUserDto
import com.engineerfred.easyrent.data.mappers.toUserInfoEntity
import com.engineerfred.easyrent.data.remote.dto.ExpenseDto
import com.engineerfred.easyrent.data.remote.dto.PaymentDto
import com.engineerfred.easyrent.data.remote.dto.RoomDto
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.data.remote.dto.UserDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.data.resource.authSafeCall
import com.engineerfred.easyrent.data.resource.safeCall
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.domain.repository.AuthRepository
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.util.compressAndConvertToByteArray
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val cache: CacheDatabase,
    private val prefs: PreferencesRepository
) : AuthRepository {

    private val auth = supabaseClient.auth
    private val storage = supabaseClient.storage

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun signUpUser(user: User, password: String, contentResolver: ContentResolver): Resource<Unit> = authSafeCall(logTag = TAG, auth) {
        Log.i(TAG, "Signing up user with email....")
        auth.signUpWith(Email) {
            this.email = user.email
            this.password = password
        }

        val loggedInUser = auth.currentUserOrNull()

        if( loggedInUser != null ) {
            Log.i(TAG, "Sign up successful! Saving userID in preferences....")
            try {
                prefs.saveUserId(loggedInUser.id)
                Log.i(TAG, "User id saved successfully! Adding user in database...")
                if( user.imageUrl.isNullOrEmpty() ) {
                    val result = supabaseClient.from(USERS).insert(
                        user.copy(id = loggedInUser.id).toUserDto()
                    ) {
                        select()
                    }.decodeSingleOrNull<UserDto>()
                    Log.i(TAG, "User saved successfully in remote database! Caching user locally...")

                    result?.let {
                        cache.userInfoDao().insertUserInfo(result.toUserInfoEntity())
                    }
                    Log.i(TAG, "User cached successfully")
                    Resource.Success(Unit)
                } else {
                    val uploadedImageUrl = uploadImage(contentResolver, loggedInUser.id, user)
                    if( !uploadedImageUrl.isNullOrEmpty() ) {
                        Log.i(TAG, "Saving user in database...")
                        val result = supabaseClient.from(USERS).insert(
                            user.copy(id = loggedInUser.id, imageUrl = uploadedImageUrl).toUserDto()
                        ) {
                            select()
                        }.decodeSingleOrNull<UserDto>()
                        Log.i(TAG, "User saved successfully in remote database! Caching user locally...")

                        result?.let {
                            cache.userInfoDao().insertUserInfo(result.toUserInfoEntity())
                        }
                        Log.i(TAG, "User saved successfully")
                        Resource.Success(Unit)
                    } else {
                        Resource.Error("Something went wrong!...Uploaded imageUrl is null")
                    }
                }
            } catch (ex: Exception) {
                Resource.Error("Signup was success but adding user to database failed due to ${ex.message}")
            }

        } else {
            Log.i(TAG, "Signup just failed! :( User not logged in!")
            Resource.Error("Signup failed! User not logged in!")
        }
    }

    override suspend fun signInUser(email: String, password: String): Resource<Unit> = authSafeCall( logTag = TAG, auth ) {
        Log.i(TAG, "Signing in user...!")
        auth.signInWith(Email){
            this.email = email
            this.password = password
        }

        //TODO("When we get an exception a user should be signed out")

        val loggedInUser = auth.currentUserOrNull()

        if ( loggedInUser != null ) {
            Log.v("BIG", "ID if the user who just got signed it: ${loggedInUser.id}")
            Log.i(TAG, "Logged in successfully! Saving userID in preferences....")
            prefs.saveUserId(loggedInUser.id)
            Log.i(TAG, "User id saved successfully! Resetting cache...")
            //cache.clearCache()
            resetDb()
            Log.i(TAG, "Fetching user info from remote database...!")
            val result1 = supabaseClient.from(USERS).select{
                filter { eq("id", loggedInUser.id) }
            }.decodeSingleOrNull<UserDto>()

            val result2 = supabaseClient.from(ROOMS).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<RoomDto>()

            val result3 = supabaseClient.from(TENANTS).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<TenantDto>()

            val result4 = supabaseClient.from(PAYMENTS).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<PaymentDto>()

            val result5 = supabaseClient.from(EXPENSES).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<ExpenseDto>()

            if (result1 != null) {
                Log.i(TAG, "User info fetched from remote database! Caching user info...")
                cache.updateCache(
                    result1.toUserInfoEntity(),
                    result2.map { it.toRoomEntity() },
                    result3.map { it.toTenantEntity() },
                    result4.map { it.toPaymentEntity() },
                    result5.map { it.toExpenseEntity() }
                )
                Log.i(TAG, "User info Cached successfully!")
            } else {
                auth.signOut()
                Resource.Error("User not found!")
            }

            Log.i(TAG, "Successfully signed in user!")
            Resource.Success(Unit)
        } else {
            Resource.Error("Something went wrong!")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun signOut(): Resource<Unit> = safeCall(currentUserId = auth.currentUserOrNull()?.id, logTag = TAG) {
        Log.i(TAG, "Logging out user...")

        auth.signOut()
        Log.i(TAG, "Successfully logged out user! Clearing cache...")

        resetDb()
        Log.i(TAG, "Successfully cleared cache! Clearing preferences...")

        prefs.clearUserId()
        Log.i(TAG, "Sign out SUCCESSFUL")

        Resource.Success(Unit)
    }

    private fun resetDb() {
        cache.runInTransaction {
            runBlocking {
                cache.clearAllTables()
            }
        }
    }

    private suspend fun uploadImage(contentResolver: ContentResolver, loggedInUserId: String, user: User) : String? {
        Log.i(TAG, "Uploading user image...")
        return withContext(NonCancellable) {
            try {
                val imageUri = user.imageUrl!!.toUri()
                val fileName = "${loggedInUserId}_${System.currentTimeMillis()}_IMG.png"

                // Convert and compress the image
                val compressedImage = imageUri.compressAndConvertToByteArray(contentResolver)

                val uploadedImgUrl = storage.from("users-images").upload(
                    path = fileName,
                    data = compressedImage,
                ) {
                    upsert = true
                    contentType = ContentType.Image.PNG
                }
                Log.i(TAG, "Upload successfully! Image path: ${buildImageUrl(uploadedImgUrl.path)}")
                buildImageUrl(uploadedImgUrl.path)
            }catch (ex: Exception) {
                Log.e(TAG, "Upload failed! Error: $ex")
                null
            }
        }
    }

    private fun buildImageUrl(imageFileName: String) =
        "$SUPABASE_URL/storage/v1/object/users-images/${imageFileName}"

}