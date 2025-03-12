package com.engineerfred.easyrent.data.worker

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.R
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.TenantEntity
import com.engineerfred.easyrent.data.mappers.toTenantDto
import com.engineerfred.easyrent.data.mappers.toTenantEntity
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
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
        setForeground(createForeGroundInfo())
        Log.wtf("MyWorker", "TenantsWorker started!")

        val userId = prefs.getUserId().firstOrNull()

        if ( userId != null ) {
            val unsyncedTenants = cache.tenantsDao().getUnsyncedTenants()

            val locallyDeletedTenants = cache.tenantsDao().getAllLocallyDeletedTenants()
            if ( locallyDeletedTenants.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedTenants.size} locally deleted tenants in cache!!")
                deleteTenantsFromSupabaseAndUpdateCache(locallyDeletedTenants, userId)
            } else {
                Log.i(TAG, "Found no locally deleted tenants in cache!!")
            }

            if ( unsyncedTenants.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedTenants.size} unsynced tenants in cache!!")
                addTenantsInSupabase(unsyncedTenants)
            } else {
                Log.i(TAG, "Found no unsynced tenants in cache!!")
            }

            //removeNotification()
            return Result.success()
        } else {
            Log.i(TAG, "User Id is null")
            //removeNotification()
            return Result.failure()

        }
    }

    private suspend fun deleteTenantsFromSupabaseAndUpdateCache(locallyDeletedTenants: List<TenantEntity>, userId: String){
        try {
            Log.i(TAG, "Deleting tenants...")
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
            Log.i(TAG, "Tenants deleted successfully!")
        } catch (ex: Exception) {
            Log.e(TAG, "Error syncing tenants (deleting): $ex")
        }
    }

    private suspend fun addTenantsInSupabase(unsyncedTenants: List<TenantEntity>){
        try {
            Log.d(TAG, "Adding unsynced tenants to supabase...")
            val remoteTenants = client.from(TENANTS).upsert(unsyncedTenants.map { it.copy(isSynced = true).toTenantDto() }){
                select()
            }.decodeList<TenantDto>()
            Log.d(TAG, "Tenants added successfully to supabase! Updating tenants in cache...")
            cache.tenantsDao().insertRemoteTenants(remoteTenants.map { it.toTenantEntity() })
            Log.i(TAG, "Tenants updated successfully in cache as well! Sync complete.")
        } catch (ex: Exception) {
            Log.d(TAG, "Error syncing tenants (insertion): $ex")
        }
    }


    private fun createForeGroundInfo() : ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "tenants_channel")
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setTicker("Tenants")
            .setContentText("Syncing tenants...")
            .setSmallIcon(R.drawable.k_logo)
            .setOngoing(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

//    private fun removeNotification() {
//        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(NOTIFICATION_ID)
//    }

}

