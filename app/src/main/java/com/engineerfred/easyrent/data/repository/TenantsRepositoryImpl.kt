package com.engineerfred.easyrent.data.repository

import android.content.ContentResolver
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.core.net.toUri
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.constants.Constants.SUPABASE_URL
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toRoomEntity
import com.engineerfred.easyrent.data.mappers.toTenant
import com.engineerfred.easyrent.data.mappers.toTenantDto
import com.engineerfred.easyrent.data.mappers.toTenantEntity
import com.engineerfred.easyrent.data.remote.dto.RoomDto
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.data.resource.safeCall
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.domain.repository.TenantsRepository
import com.engineerfred.easyrent.util.compressAndConvertToByteArray
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TenantsRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val cache: CacheDatabase,
    private val storage: Storage,
    private val prefs: PreferencesRepository
) : TenantsRepository {

    companion object {
        private const val COLUMN_ID = "id"
        private const val ROOM_ID = "roomId"
        private const val TAG = "TenantsRepositoryImpl"
    }

    private val currentUser = supabaseClient.auth.currentUserOrNull()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun insertTenant(tenant: Tenant, contentResolver: ContentResolver): Resource<Unit> = safeCall(currentUserId = currentUser?.id, logTag = TAG){
        Log.i("C_TENANTS", "Inserting tenant in cache....")
        val userIdInPrefs = prefs.getUserId().firstOrNull()

        if ( !userIdInPrefs.isNullOrEmpty() && userIdInPrefs == currentUser?.id ) {
            if( !tenant.profilePic.isNullOrEmpty() ) {
                val uploadedImageUrlResult = uploadImage(contentResolver, tenant, userIdInPrefs)
                when(uploadedImageUrlResult) {
                    is Resource.Error -> {
                        return@safeCall  Resource.Error(uploadedImageUrlResult.msg)
                    }
                    Resource.Loading -> Unit
                    is Resource.Success -> {
                        val uploadedImageUrl = uploadedImageUrlResult.data
                        if( !uploadedImageUrl.isNullOrEmpty() ) {
                            //1.
                            cache.tenantsDao().insertTenant(tenant.copy(profilePic = uploadedImageUrl, userId =  userIdInPrefs).toTenantEntity())
                            Log.v(TAG, "Next: Updating room occupancy status to true in cache....")
                            cache.roomsDao().updateRoomStatus(tenant.roomId, true)
                            Log.v(TAG, "Local cache complete! Inserting tenant in supabase...")

                            //2.
                            try {
                                //a.
                                val supabaseTenant = tenant.copy(isSynced = true, profilePic = uploadedImageUrl, userId = userIdInPrefs).toTenantDto()
                                val resultTenant = supabaseClient.from(TENANTS).insert(supabaseTenant){
                                    select()
                                }.decodeSingleOrNull<TenantDto>()

                                if( resultTenant != null ) {
                                    //b.
                                    Log.i(TAG, "Next: Updating tenant sync status in cache to true...")
                                    cache.tenantsDao().updateTenant(resultTenant.toTenantEntity())

                                    //c.
                                    Log.v(TAG, "Next: Updating room occupancy status to true in supabase")
                                    val updatedRoom = supabaseClient.from(ROOMS).update(
                                        {
                                            set("is_occupied", true)
                                        }
                                    ) {
                                        select()
                                        filter {
                                            eq("id", tenant.roomId)
                                            eq("user_id", currentUser.id)
                                        }
                                    }.decodeSingleOrNull<RoomDto>()

                                    updatedRoom?.let {
                                        Log.v(TAG, "DONE!! updating room sync status to true in cache...")
                                        cache.roomsDao().updateRoom(updatedRoom.toRoomEntity())
                                        Log.v(TAG, "Everything is done!")
                                    }
                                } else {
                                    Log.e(TAG, "Error inserting tenant in supabase!")
                                }

                            }catch (ex: Exception) {
                                Log.e(TAG, "Failed to sync with Supabase. Error: ${ex.message}", ex)
                                Resource.Error("$ex")
                            }

                            Resource.Success(Unit)

                        } else {
                            Log.v(TAG, "Unable to upload image!")
                            Resource.Error("Failed to upload image")
                        }
                    }
                }

            } else {

                //1.
                val addedTenantId = cache.tenantsDao().insertTenant(tenant.copy(userId = userIdInPrefs).toTenantEntity())
                Log.v(TAG, "Next: Updating room occupancy status to true in cache.... Btw the new created tenant id is $addedTenantId")
                cache.roomsDao().updateRoomStatus(tenant.roomId, true) //not yet synced
                Log.v(TAG, "Local cache complete! Inserting tenant in cloud...")

                //2.
                try {

                    //1.
                    val supabaseTenant = tenant.copy(isSynced = true, userId = userIdInPrefs).toTenantDto()
                    val resultTenant = supabaseClient.from(TENANTS).insert(supabaseTenant){
                        select()
                    }.decodeSingleOrNull<TenantDto>()

                    if ( resultTenant != null ) {
                        Log.i(TAG, "Next: Updating tenant sync status in cache to true...")
                        cache.tenantsDao().updateTenant(resultTenant.toTenantEntity())

                        Log.v(TAG, "Next: Updating room occupancy status to true in supabase...")
                        val updatedRoom = supabaseClient.from(ROOMS).update(
                            {
                                set("is_occupied", true)
                            }
                        ) {
                            select()
                            filter {
                                eq("id", tenant.roomId)
                                eq("user_id", currentUser.id)
                            }
                        }.decodeSingleOrNull<RoomDto>()

                        updatedRoom?.let {
                            Log.v(TAG, "DONE!! updating room sync status to true in cache...")
                            cache.roomsDao().updateRoom(updatedRoom.copy(isSynced = true).toRoomEntity())
                            Log.v(TAG, "Everything was successful")
                        }
                    } else {
                        Log.e(TAG, "Error inserting tenant in supabase!")
                    }

                }catch (ex: Exception) {
                    Log.e(TAG, "Failed to sync with Supabase. Error: ${ex.message}", ex)
                }
            }
            Log.i(TAG, "Tenant inserted successfully! (Cached)")
            Resource.Success(Unit)
        } else {
            Resource.Error("BAD REQUEST!")
        }

    }

//    override suspend fun updateTenant(tenant: Tenant): Resource<Unit> = safeCall {
//        cache.tenantsDao().insertTenant(tenant.copy(isSynced = false).toTenantEntity())
//        supabaseClient.from(TENANTS).update(tenant){
//            filter { eq(COLUMN_ID, tenant.id) }
//        }
//        cache.tenantsDao().insertTenant(tenant.copy(isSynced = true).toTenantEntity())
//        Resource.Success(Unit)
//    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun deleteTenant(tenant: Tenant): Resource<Unit> = safeCall(currentUserId = currentUser?.id, logTag = TAG) {
        Log.i(TAG, "Marking tenant as deleted in cache...")
        val userIdInPrefs = prefs.getUserId().firstOrNull()
        if ( userIdInPrefs == tenant.userId && tenant.userId == currentUser!!.id ) {
            //cache.tenantsDao().updateTenant(tenant.copy(isDeleted = true, isSynced = false).toTenantEntity())
            cache.tenantsDao().markTenantAsDeleted(tenant.id)
            Log.i(TAG, "Successfully marked tenant as deleted in cache! Updating room occupancy status to false in cache...")
            cache.roomsDao().updateRoomStatus(tenant.roomId, false)
            Log.i(TAG, "room occupancy status updated successfully to false! Deleting tenant from supabase....")

            try {
                supabaseClient.from(TENANTS).delete{
                    filter {
                        eq(COLUMN_ID, tenant.id)
                        eq("user_id", currentUser.id)
                    }
                }
                Log.i(TAG, "Tenant deleted successfully from supabase! Deleting tenant permanently from cache...")
                cache.tenantsDao().deleteTenant(tenant.id)
                Log.i(TAG, "Successfully delete tenant from cache! Updating room occupancy status in supabase....")
                val updatedRoom = supabaseClient.from(ROOMS).update(
                    {
                        set("is_occupied", false)
                    }
                ) {
                    select()
                    filter {
                        eq("id", tenant.roomId)
                        eq("user_id", currentUser.id)
                    }
                }.decodeSingleOrNull<RoomDto>()

                Log.i(TAG, "Room occupancy status successfully updated in supabase to true. Updating cache.....")
                updatedRoom?.let {
                    cache.roomsDao().updateRoom(updatedRoom.toRoomEntity())
                }
                Log.i(TAG, "Everything is done!")
            }catch (ex: Exception) {
                Log.e(TAG, "Error from supabase: ${ex.message}")
                throw  ex
            }
            Resource.Success(Unit)
        } else {
            Log.i(TAG, "Can't delete tenant")
            Resource.Error("You can't delete that tenant!")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getTenantInRoom(roomId: String): Resource<Tenant?> = safeCall( currentUserId = currentUser?.id, logTag = TAG) {
        Log.i(TAG, "Fetching room tenant from cache.....")
        val localTenant = cache.tenantsDao().getTenantInRoom(roomId)
        if( localTenant != null ) {
            Log.i(TAG, "Tenant successfully fetched from cache.....")
            return@safeCall Resource.Success(localTenant.toTenant())
        }
        Log.i(TAG, "Tenant not found in cache! Fetching room tenant from supabase.....")
        try {
            val remoteTenant = supabaseClient.from(TENANTS).select{
                filter {
                    eq(ROOM_ID, roomId)
                    eq("user_id", currentUser!!.id)
                }
            }.decodeSingleOrNull<TenantDto>()

            if(remoteTenant != null ) {
                Log.i(TAG, "Tenant found in supabase! Now caching.....")
                cache.tenantsDao().insertTenant(remoteTenant.toTenantEntity())
                return@safeCall Resource.Success(remoteTenant.toTenant())
            }
        }catch (e: Exception) {
            Log.e(TAG, "Error fetching tenant in room (Supabase)! ${e.message}")
        }

        Log.i(TAG, "Tenant not found!")
        Resource.Success(null)
    }

    override fun getAllTenants(): Flow<Resource<List<Tenant>>> = flow {
        if ( currentUser != null ) {
            Log.v(TAG, "Fetching local tenants from cache...")
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            val userId = if ( userIdInPrefs == currentUser.id ) currentUser.id else userIdInPrefs
            val localFlow = cache.tenantsDao().getAllTenants(userId ?: currentUser.id)
                .map { localTenants ->
                    Log.v(TAG, "Found ${localTenants.size} tenants in cache!")
                    if (localTenants.isEmpty()) {
                        Log.v(TAG, "No tenants in cache! Fetching from Supabase...")
                        try {
                            val remoteTenants = supabaseClient.from(TENANTS).select{
                                filter {
                                    eq("user_id", userId ?: currentUser.id)
                                }
                            }.decodeList<TenantDto>()
                            if (remoteTenants.isNotEmpty()) {
                                Log.v(TAG, "Fetched ${remoteTenants.size} tenants from Supabase! Now caching....")
                                remoteTenants.forEach {
                                    val tenantRoom = cache.roomsDao().getRoomById(it.roomId).firstOrNull()
                                    tenantRoom?.let { room ->
                                        cache.roomsDao().updateRoom(room.copy(isSynced = false, isOccupied = true))
                                        val result = supabaseClient.from(ROOMS).update(
                                            {
                                                set("is_occupied", true)
                                                set("is_synced", true)
                                            }
                                        ){
                                            select()
                                            filter {
                                                eq("id", room.id)
                                                eq("user_id", userIdInPrefs ?: currentUser.id)
                                            }
                                        }.decodeSingleOrNull<RoomDto>()

                                        result?.let {
                                            cache.roomsDao().updateRoom(result.toRoomEntity())
                                        }
                                    }
                                }
                                cache.tenantsDao().insertRemoteTenants(
                                    remoteTenants.map { it.toTenantEntity() }
                                )
                                Log.v(TAG, "Tenants cached successfully!")
                            } else {
                                Log.v(TAG, "No tenants in cloud too!")
                            }
                        } catch (ex: Exception) {
                            Log.e(TAG, "Supabase error: ${ex.message}", ex)
                        }
                    }
                    Resource.Success(localTenants.map { it.toTenant() })
                }
            emitAll(localFlow)
        } else {
            Log.i(TAG, "User not logged in!")
            emit(Resource.Error("User not logged in!"))
        }

    }.flowOn(Dispatchers.IO)

//    override suspend fun getTenantById(tenantId: Int): Resource<Tenant?> = safeCall {
//        val localTenant = cache.tenantsDao().getTenantById(tenantId)
//        if( localTenant != null ) {
//            return@safeCall Resource.Success(localTenant.toTenant())
//        }
//        val remoteTenant = supabaseClient.from(TENANTS).select{
//            filter { eq(COLUMN_ID, tenantId) }
//        }.decodeSingleOrNull<TenantDto>()
//
//        remoteTenant?.let { cache.tenantsDao().insertTenant(it.copy(isSynced = true).toTenantEntity()) }
//        Resource.Success(remoteTenant?.toTenant())
//    }

    private suspend fun uploadImage(contentResolver: ContentResolver, tenant: Tenant, userId: String) : Resource<String?> {
        if( !tenant.profilePic.isNullOrEmpty() ) {
            Log.i(TAG, "Uploading image...")
            return withContext(NonCancellable) {
                try {
                    val imageUri = tenant.profilePic.toUri()
                    val fileName = "${userId}_${System.currentTimeMillis()}_IMG.png"

                    // Convert and compress the image
                    val compressedImage = imageUri.compressAndConvertToByteArray(contentResolver)

                    val uploadedImgUrl = storage.from("tenants-images").upload(
                        path = fileName,
                        data = compressedImage,
                    ) {
                        upsert = true
                        contentType = ContentType.Image.PNG
                    }
                    Log.i(TAG, "Upload successfully! Image path: ${buildImageUrl(uploadedImgUrl.path)}")
                    Resource.Success(buildImageUrl(uploadedImgUrl.path))
                }catch (ex: Exception) {
                    Log.e(TAG, "Upload failed! Error: $ex")
                    Resource.Error("$ex")
                }
            }
        } else {
            Log.i(TAG, "Image Url is empty!")
            return Resource.Success("")
        }
    }

    private fun buildImageUrl(imageFileName: String) =
        "${SUPABASE_URL}/storage/v1/object/tenants-images/${imageFileName}"
}
