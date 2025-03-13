package com.engineerfred.easyrent.data.repository

import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toRoomEntity
import com.engineerfred.easyrent.data.mappers.toTenant
import com.engineerfred.easyrent.data.mappers.toTenantDto
import com.engineerfred.easyrent.data.mappers.toTenantEntity
import com.engineerfred.easyrent.data.remote.dto.RoomDto
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.domain.repository.TenantsRepository
import com.engineerfred.easyrent.util.buildImageUrl
import com.engineerfred.easyrent.util.compressAndConvertToByteArray
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
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

    override suspend fun insertTenant(tenant: Tenant, contentResolver: ContentResolver): Resource<Unit> {
        try {

            val userId = prefs.getUserId().firstOrNull()

            if( userId == null ) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            // Upload image if exists
            val imageUrl = if (!tenant.profilePic.isNullOrEmpty()) {
                uploadImage(contentResolver,tenant, userId)
            } else null

            //insert the tenant
            cache.tenantsDao().insertTenant(tenant.copy(profilePic = imageUrl, userId = userId).toTenantEntity())
            Log.v(TAG, "Updating room occupancy status to true in cache....")

            //updating room
            cache.roomsDao().updateRoomStatus(tenant.roomId, true)
            Log.v(TAG, "Local cache complete! Inserting tenant in supabase...")

            try {

                val supabaseTenant = tenant.copy(isSynced = true, profilePic = imageUrl, userId = userId).toTenantDto()
                val resultTenant = supabaseClient.from(TENANTS).insert(supabaseTenant){
                    select()
                }.decodeSingleOrNull<TenantDto>()

                val resultUpdatedRoom = supabaseClient.from(ROOMS).update(
                    {
                        set("is_occupied", true)
                    }
                ) {
                    select()
                    filter {
                        eq("id", tenant.roomId)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<RoomDto>()

                resultTenant?.let { cache.tenantsDao().updateTenant(resultTenant.toTenantEntity()) }
                resultUpdatedRoom?.let { cache.roomsDao().updateRoom(resultUpdatedRoom.toRoomEntity()) }

            } catch (ex: Exception){
                Log.e(TAG, "Error updating supabase: ${ex.message}")
            }

            Log.i(TAG, "Tenant inserted successfully!")
            return Resource.Success(Unit)

        }catch (ex: Exception) {
            Log.e(TAG, "Error inserting tenant: ${ex.message}")
            return Resource.Error("Error inserting tenant: ${ex.message}")
        }
    }

    override suspend fun deleteTenant(tenant: Tenant): Resource<Unit> {

        try {

            val userId = prefs.getUserId().firstOrNull()
            if( userId == null ) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            if ( userId != tenant.userId ) {
                Log.i(TAG, "Can't delete tenant")
                return Resource.Error("Can't delete tenant!")
            }

            cache.tenantsDao().markTenantAsDeleted(tenant.id)
            Log.i(TAG, "Successfully marked tenant as deleted in cache! Updating room occupancy status to false in cache...")

            cache.roomsDao().updateRoomStatus(tenant.roomId, false)
            Log.i(TAG, "room occupancy status updated successfully to false! Deleting tenant from supabase....")

            try {
                supabaseClient.from(TENANTS).delete{
                    filter {
                        eq(COLUMN_ID, tenant.id)
                        eq("user_id", userId)
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
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<RoomDto>()

                Log.i(TAG, "Room occupancy status successfully updated in supabase to true. Updating cache.....")
                updatedRoom?.let { cache.roomsDao().updateRoom(updatedRoom.toRoomEntity()) }
                Log.i(TAG, "Everything is done!")
            }catch (ex: Exception) {
                Log.e(TAG, "Error from supabase: ${ex.message}")
            }

            Log.i(TAG, "Tenant deleted successfully!")
            return Resource.Success(Unit)

        }catch (ex: Exception) {
            Log.e(TAG, "Error deleting tenant: ${ex.message}")
            return Resource.Error("Error deleting tenant: ${ex.message}")
        }
    }

    override suspend fun getTenantInRoom(roomId: String): Resource<Tenant?> {
        try {
            val userId = prefs.getUserId().firstOrNull()

            if( userId == null ) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            Log.i(TAG, "Fetching room tenant from cache.....")
            val localTenant = cache.tenantsDao().getTenantInRoom(roomId)

            if( localTenant != null ) {
                Log.i(TAG, "Tenant successfully fetched from cache")
                return Resource.Success(localTenant.toTenant())
            }

            Log.i(TAG, "Tenant not found in cache! Fetching room tenant from supabase.....")
            try {
                val remoteTenant = supabaseClient.from(TENANTS).select{
                    filter {
                        eq(ROOM_ID, roomId)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<TenantDto>()

                if(remoteTenant != null ) {
                    Log.i(TAG, "Tenant found in supabase! Now caching.....")
                    cache.tenantsDao().insertTenant(remoteTenant.toTenantEntity())
                    return Resource.Success(remoteTenant.toTenant())
                }
            }catch (e: Exception) {
                Log.e(TAG, "Error fetching tenant from supabase: ${e.message}")
            }

            Log.i(TAG, "Tenant not found!")
            return Resource.Success(null)
        } catch (ex: Exception) {
            Log.e(TAG, "Error fetching tenant in room: ${ex.message}")
            return Resource.Error("Error fetching tenant in room: ${ex.message}")
        }
    }

    override fun getAllTenants(): Flow<Resource<List<Tenant>>> = flow {

        val userId = prefs.getUserId().firstOrNull() ?: run {
            emit(Resource.Error("User not logged in!"))
            return@flow
        }

        Log.v(TAG, "Fetching local tenants from cache...")

        val localFlow = cache.tenantsDao().getAllTenants(userId)
            .map { localTenants ->
                Log.v(TAG, "Found ${localTenants.size} tenants in cache!")
                if (localTenants.isEmpty()) {
                    Log.v(TAG, "No tenants in cache! Fetching from Supabase...")

                    try {
                        val remoteTenants = supabaseClient.from(TENANTS).select{
                            filter {
                                eq("user_id", userId)
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
                                            eq("user_id", userId)
                                        }
                                    }.decodeSingleOrNull<RoomDto>()

                                    result?.let { cache.roomsDao().updateRoom(result.toRoomEntity()) }
                                }
                            }
                            cache.tenantsDao().insertRemoteTenants( remoteTenants.map { it.toTenantEntity() })
                            Log.v(TAG, "Tenants cached successfully!")
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Supabase error: ${ex.message}", ex)
                    }
                }
                Resource.Success(localTenants.map { it.toTenant() })
            }
        emitAll(localFlow)

    }.flowOn(Dispatchers.IO).catch {
        Log.e(TAG, "Error fetching tenants: ${it.message}", it)
        emit(Resource.Error("Error fetching tenants: ${it.message}"))
    }.distinctUntilChanged()

    private suspend fun uploadImage(contentResolver: ContentResolver, tenant: Tenant, userId: String) : String {
        Log.i(TAG, "Uploading image...")
        return withContext(NonCancellable) {
            try {
                val imageUri = tenant.profilePic!!.toUri()
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
                buildImageUrl(uploadedImgUrl.path)
            }catch (ex: Exception) {
                Log.e(TAG, "Upload failed! Error: $ex")
                throw ex
            }
        }
    }
}
