package com.engineerfred.easyrent.data.repository

import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import com.engineerfred.easyrent.constants.Constants.EXPENSES
import com.engineerfred.easyrent.constants.Constants.PAYMENTS
import com.engineerfred.easyrent.constants.Constants.ROOMS
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
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.domain.repository.AuthRepository
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.util.buildImageUrl
import com.engineerfred.easyrent.util.compressAndConvertToByteArray
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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

    override suspend fun signUpUser(
        user: User,
        password: String,
        contentResolver: ContentResolver
    ): Resource<Unit> {
        try {
            Log.i(TAG, "Signing up user with email....")

            auth.signUpWith(Email) {
                this.email = user.email
                this.password = password
            }

            val loggedInUser = auth.currentUserOrNull()
            if (loggedInUser == null) {
                Log.e(TAG, "Signup failed! User not logged in!")
                return Resource.Error("Signup failed! User not logged in!")
            }

            Log.i(TAG, "Sign up successful! Saving userID in preferences....")
            prefs.saveUserId(loggedInUser.id)

            // Upload image if exists
            val uploadedImageUrl = if (!user.imageUrl.isNullOrEmpty()) {
                uploadImage(contentResolver, loggedInUser.id, user)
            } else null

            // Insert user into Supabase
            val result = supabaseClient.from(USERS).insert(
                user.copy(id = loggedInUser.id, imageUrl = uploadedImageUrl)
                    .toUserDto()
            ) {
                select()
            }.decodeSingleOrNull<UserDto>()

            if (result == null) {
                Log.e(TAG, "User insertion failed in the remote database!")
                prefs.clearUserId()
                return Resource.Error("Failed to save user in database")
            }

            Log.i(TAG, "User saved successfully in remote database! Caching user locally...")

            cache.userInfoDao().insertUserInfo(result.toUserInfoEntity())

            Log.i(TAG, "User cached successfully")
            return Resource.Success(Unit)

        } catch (ex: Exception) {
            Log.e(TAG, "Error signing up user: ${ex.message}")
            prefs.clearUserId()
            return Resource.Error("Error signing up user: ${ex.message}")
        }
    }


    override suspend fun signInUser(email: String, password: String): Resource<Unit> {
        try {
            Log.i(TAG, "Signing in user...")

            auth.signInWith(Email){
                this.email = email
                this.password = password
            }

            val loggedInUser = auth.currentUserOrNull()

            if (loggedInUser == null) {
                Log.e(TAG, "Login failed! User not logged in!")
                return Resource.Error("Login failed! User not logged in!")
            }

            Log.i(TAG, "Logged in successfully! Saving userID in preferences....")
            prefs.saveUserId(loggedInUser.id)

            Log.i(TAG, "User id saved successfully! Resetting cache...")
            resetDb() //resetting db. Deleting all previous cached data

            Log.i(TAG, "Fetching user info from remote database...")
            val currentUser = supabaseClient.from(USERS).select{
                filter { eq("id", loggedInUser.id) }
            }.decodeSingleOrNull<UserDto>()

            if (currentUser == null) {
                prefs.clearUserId()
                return Resource.Error("User not found!")
            }

            val userRooms = supabaseClient.from(ROOMS).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<RoomDto>()

            val userTenants = supabaseClient.from(TENANTS).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<TenantDto>()

            val userPayments = supabaseClient.from(PAYMENTS).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<PaymentDto>()

            val userExpenses = supabaseClient.from(EXPENSES).select{
                filter { eq("user_id", loggedInUser.id) }
            }.decodeList<ExpenseDto>()

            Log.i(TAG, "User info fetched from remote database! Caching user info...")
            cache.updateCache(
                currentUser.toUserInfoEntity(),
                userRooms.map { it.toRoomEntity() },
                userTenants.map { it.toTenantEntity() },
                userPayments.map { it.toPaymentEntity() },
                userExpenses.map { it.toExpenseEntity() }
            )
            Log.i(TAG, "User info Cached successfully!")

            Log.i(TAG, "Sign in successful!")
            return Resource.Success(Unit)
        }catch (ex: Exception) {
            Log.e(TAG, "Error signing in user: ${ex.message}")
            prefs.clearUserId()
            return Resource.Error("Error signing in user: ${ex.message}")
        }
    }

    override suspend fun signOut(): Resource<Unit>  {
        try {
            Log.i(TAG, "Logging out user...")

            auth.signOut()
            Log.i(TAG, "Successfully logged out user! Clearing cache...")

            prefs.clearUserId()
            Log.i(TAG, "Successfully cleared preferences. Clearing cache...")

            resetDb()
            Log.i(TAG, "Sign out SUCCESSFUL")

            return Resource.Success(Unit)
        }catch (ex: Exception) {
            Log.e(TAG, "Error signing out user: ${ex.message}")
            return Resource.Error("Error signing out user: ${ex.message}")
        }
    }

    private suspend fun resetDb() {
        withContext(Dispatchers.IO) {
            cache.clearAllTables()
        }
    }

    private suspend fun uploadImage(contentResolver: ContentResolver, loggedInUserId: String, user: User) : String {
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

                buildImageUrl(uploadedImgUrl.path, "users-images")
            }catch (ex: Exception) {
                throw ex
            }
        }
    }
}