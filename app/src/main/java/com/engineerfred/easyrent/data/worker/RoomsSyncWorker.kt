package com.engineerfred.easyrent.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.RoomEntity
import com.engineerfred.easyrent.data.mappers.toRoomDto
import com.engineerfred.easyrent.data.mappers.toRoomEntity
import com.engineerfred.easyrent.data.remote.dto.RoomDto
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
        Log.wtf("MyWorker", "RoomsWorker started!")

        try {
            val userId = prefs.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User ID is null. Cannot sync rooms.")
                return Result.failure()
            }

            setForeground(
                createForeGroundInfo(
                    applicationContext,
                    NOTIFICATION_ID,
                    ChannelNames.RoomsChannel.name,
                    "Syncing rooms..."
                )
            )

            val locallyDeletedRooms = cache.roomsDao().getAllLocallyDeletedRooms(userId)
            if ( locallyDeletedRooms.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedRooms.size} locally deleted rooms in cache!!")
                deleteRoomsFromSupabaseAndUpdateCache(locallyDeletedRooms, userId)
            }

            val unsyncedRooms = cache.roomsDao().getUnsyncedRooms(userId)
            if ( unsyncedRooms.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedRooms.size} unsynced rooms in cache!!")
                addRoomsInSupabase(unsyncedRooms)
            }

            Log.i(TAG, "Rooms synced successfully!")
            cancelNotification(applicationContext, NOTIFICATION_ID)
            return Result.success()
        }catch (ex: Exception) {
            Log.e(TAG, "Error syncing rooms: ${ex.message}")
            if ( isRetryableError(ex) ) {
                return Result.retry()
            } else {
                cancelNotification(applicationContext, NOTIFICATION_ID)
                return Result.failure()
            }
        }
    }

    private suspend fun deleteRoomsFromSupabaseAndUpdateCache(locallyDeletedRooms: List<RoomEntity>, userId: String){
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
    }

    private suspend fun addRoomsInSupabase(unsyncedRooms: List<RoomEntity>){
        val remoteRooms = client.from(ROOMS).upsert(unsyncedRooms.map { it.copy(isSynced = true).toRoomDto() }){
            select()
        }.decodeList<RoomDto>()
        cache.roomsDao().upsertRooms(remoteRooms.map { it.toRoomEntity() })
    }
}