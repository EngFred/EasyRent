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
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.RoomEntity
import com.engineerfred.easyrent.data.mappers.toRoomDto
import com.engineerfred.easyrent.data.mappers.toRoomEntity
import com.engineerfred.easyrent.data.remote.dto.RoomDto
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class RoomsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val client: SupabaseClient,
    @Assisted val cache: CacheDatabase,
    @Assisted val prefs: PreferencesRepository,
) : CoroutineWorker(
    context, workerParams
) {
    companion object {
        const val TAG = "RoomsSyncWorker"
        private const val NOTIFICATION_ID = 2
    }

    override suspend fun doWork(): Result {
        setForeground(createForeGroundInfo())
        Log.wtf("MyWorker", "RoomsWorker started!")

        val userId = prefs.getUserId().firstOrNull()
        if ( userId != null ) {

            val locallyDeletedRooms = cache.roomsDao().getAllLocallyDeletedRooms(userId)
            if ( locallyDeletedRooms.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedRooms.size} locally deleted rooms in cache!!")
                deleteRoomsFromSupabaseAndUpdateCache(locallyDeletedRooms, userId)
            } else {
                Log.i(TAG, "Found no locally deleted rooms in cache!!")
            }

            val unsyncedRooms = cache.roomsDao().getUnsyncedRooms(userId)
            if ( unsyncedRooms.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedRooms.size} unsynced rooms in cache!!")
                addRoomsInSupabase(unsyncedRooms)
            } else {
                Log.i(TAG, "Found no unsynced rooms in cache!!")
            }

            //removeNotification()
            return Result.success()
        } else {
            Log.i(TAG, "User Id is null")
            //removeNotification()
            return Result.failure()
        }
    }

    private suspend fun deleteRoomsFromSupabaseAndUpdateCache(locallyDeletedRooms: List<RoomEntity>, userId: String){
        try {
            Log.i(TAG, "Deleting rooms...")
            locallyDeletedRooms.forEach {
                if ( it.isOccupied.not() ) {
                    val deletedRoom = client.from(ROOMS).delete{
                        select()
                        filter {
                            eq("id", it.id)
                            eq("user_id", userId)
                        }
                    }.decodeSingleOrNull<RoomDto>()

                    deletedRoom?.let {
                        cache.roomsDao().deleteRoom(it.id)
                    }
                }
            }
            Log.i(TAG, "Rooms deleted successfully!")
        } catch (ex: Exception) {
            Log.e(TAG, "Error syncing rooms (deleting): $ex")
        }
    }

    private suspend fun addRoomsInSupabase(unsyncedRooms: List<RoomEntity>){
        try {
            Log.d(TAG, "Adding unsynced rooms to supabase...")
            val remoteRooms = client.from(ROOMS).upsert(unsyncedRooms.map { it.copy(isSynced = true).toRoomDto() }){
                select()
            }.decodeList<RoomDto>()
            Log.d(TAG, "Rooms added successfully to supabase! Updating rooms in cache...")
            cache.roomsDao().upsertRooms(remoteRooms.map { it.toRoomEntity() })
            Log.i(TAG, "Rooms updated successfully in cache as well! Sync complete.")
        } catch (ex: Exception) {
            Log.d(TAG, "Error syncing rooms (insertion): $ex")
        }
    }

    private fun createForeGroundInfo() : ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "rooms_channel")
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setTicker("Rooms")
            .setContentText("Syncing rooms...")
            .setSmallIcon(R.drawable.k_logo)
            .setOngoing(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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