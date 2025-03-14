package com.engineerfred.easyrent.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.TenantEntity
import com.engineerfred.easyrent.data.mappers.toTenantDto
import com.engineerfred.easyrent.data.mappers.toTenantEntity
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.util.ChannelNames
import com.engineerfred.easyrent.util.WorkerUtils.cancelNotification
import com.engineerfred.easyrent.util.WorkerUtils.createForeGroundInfo
import com.engineerfred.easyrent.util.WorkerUtils.isRetryableError
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class TenantsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val client: SupabaseClient,
    @Assisted val cache: CacheDatabase,
    @Assisted val prefs: PreferencesRepository
) : CoroutineWorker(
    context, workerParams
) {
    
    companion object {
        const val TAG = "TenantsSyncWorker"
        private const val NOTIFICATION_ID  = 1
    }
    
    override suspend fun doWork(): Result {
        Log.wtf("MyWorker", "TenantsWorker started!")

        try {

            val userId = prefs.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User ID is null. Cannot sync tenants.")
                return Result.failure()
            }

            setForeground(
                createForeGroundInfo(
                    applicationContext,
                    NOTIFICATION_ID,
                    ChannelNames.TenantsChannel.name,
                    "Tenants",
                    "Syncing tenants..."
                )
            )

            val unsyncedTenants = cache.tenantsDao().getUnsyncedTenants()
            val locallyDeletedTenants = cache.tenantsDao().getAllLocallyDeletedTenants()

            if ( locallyDeletedTenants.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedTenants.size} locally deleted tenants in cache!!")
                deleteTenantsFromSupabaseAndUpdateCache(locallyDeletedTenants, userId)
            }

            if ( unsyncedTenants.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedTenants.size} unsynced tenants in cache!!")
                addTenantsInSupabase(unsyncedTenants)
            }

            cancelNotification(applicationContext, NOTIFICATION_ID)
            return Result.success()


        }catch (ex: Exception) {
            Log.e(TAG, "Error syncing tenants: ${ex.message}")
            if ( isRetryableError(ex) ) {
                return Result.retry()
            } else {
                cancelNotification(applicationContext, NOTIFICATION_ID)
                return Result.failure()
            }
        }
    }

    private suspend fun deleteTenantsFromSupabaseAndUpdateCache(locallyDeletedTenants: List<TenantEntity>, userId: String){
        locallyDeletedTenants.forEach {
            val deletedTenant = client.from(TENANTS).delete{
                select()
                filter {
                    eq("id", it.id)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<TenantDto>()

            deletedTenant?.let {
                cache.tenantsDao().deleteTenant(it.id)
                client.from(ROOMS).update(
                    { set("is_occupied", false) }
                ) {
                    filter {
                        eq("id", it.roomId)
                        eq("user_id", userId)
                    }
                }
            }
        }
    }

    private suspend fun addTenantsInSupabase(unsyncedTenants: List<TenantEntity>){
        val remoteTenants = client.from(TENANTS).upsert(unsyncedTenants.map { it.copy(isSynced = true).toTenantDto() }){
            select()
        }.decodeList<TenantDto>()
        cache.tenantsDao().insertRemoteTenants(remoteTenants.map { it.toTenantEntity() })
    }
}

