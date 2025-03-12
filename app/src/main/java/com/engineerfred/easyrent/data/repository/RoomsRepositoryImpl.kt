package com.engineerfred.easyrent.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toRoom
import com.engineerfred.easyrent.data.mappers.toRoomDTO
import com.engineerfred.easyrent.data.mappers.toRoomEntity
import com.engineerfred.easyrent.data.remote.dto.RoomDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.data.resource.safeCall
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.domain.repository.RoomsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomsRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val cache: CacheDatabase,
    private val prefs: PreferencesRepository
) : RoomsRepository {

    companion object {
        private const val COLUMN_ID = "id"
        private const val TAG = "RoomsRepositoryImpl"
    }

    private val currentUser = supabaseClient.auth.currentUserOrNull()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun insertRoom(room: Room): Resource<String>  = safeCall(currentUserId = currentUser?.id, logTag = TAG) {
        Log.i(TAG, "Inserting room in cache....")
//        val createdRoomId = try {
//            cache.roomsDao().insertRoom(room.copy(userId = currentUser!!.id).toRoomEntity())
//        } catch (e: SQLiteConstraintException) {
//            Log.e(TAG, "Room with the same number already exists.")
//            return@safeCall Resource.Error("Room with this number already exists.")
//        }
        val userIdInPrefs = prefs.getUserId().firstOrNull()
        val userId = if ( userIdInPrefs == currentUser!!.id ) currentUser.id else userIdInPrefs
        val existingRoom = cache.roomsDao().getRoomByNumber(room.roomNumber)
        if ( existingRoom == null ) {
            cache.roomsDao().insertRoom(room.copy(userId = userId ?: currentUser.id).toRoomEntity())

            try {
                val supabaseRoom = room.copy(isSynced = true, userId = userId ?: currentUser.id).toRoomDTO()
                val result = supabaseClient.from(ROOMS).insert(supabaseRoom){
                    select()
                }.decodeSingleOrNull<RoomDto>()
                Log.i(TAG, "Room inserted in Supabase successfully.")

                result?.let {
                    cache.roomsDao().updateRoom(result.toRoomEntity())
                    Log.i(TAG, "Room cached successfully as well!")
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to sync with Supabase. Error: ${ex.message}", ex)
                Resource.Success(Unit) // returning success since the room was already cached
            }

            Log.i(TAG, "Room insertion completed successfully and (cached).")
            Resource.Success(room.id) // Always return success for the cached room
        } else {
            //TODO("We shall assume the person is updating room")
            Log.e(TAG, "Room with the same number already exists.")
            return@safeCall Resource.Error("Room with this number already exists.")
        }

    }

//    override suspend fun updateRoom(room: Room): Resource<Unit> = safeCall {
//        cache.roomsDao().updateRoom(room.toRoomEntity())
//        supabaseClient.from(ROOMS).update(room.toRoomDTO()){
//            filter { eq(COLUMN_ID, room.id) }
//        }
//        Resource.Success(Unit)
//    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun deleteRoom(room: Room): Resource<Unit> = safeCall(currentUserId = currentUser?.id, logTag = TAG) {
        //TODO("Ensure there's no tenant in the room")
        val userIdInPrefs = prefs.getUserId().firstOrNull()
        if ( currentUser!!.id == userIdInPrefs && room.userId == currentUser.id ) {
            Log.i(TAG, "Marking room as deleted in cache...")
            cache.roomsDao().markRoomAsDeleted(room.id)
            Log.i(TAG, "Successfully marked room as deleted in cache! Deleting from room from supabase....")
            supabaseClient.from(ROOMS).delete {
                filter {
                    eq(COLUMN_ID, room.id)
                    eq("user_id", currentUser.id)
                }
            }
            Log.i(TAG, "Room deleted successfully from cloud! Now permanently deleting room from cache...")
            cache.roomsDao().deleteRoom(room.id)
            Log.i(TAG, "Permanently deleted room from cache!")
            Resource.Success(Unit)
        } else {
            Resource.Error("Can't delete that room!")
        }
    }

    override fun getAllRooms(): Flow<Resource<List<Room>>> = flow {
        if ( currentUser != null ) {
            Log.i("BIG", "Current loggedIn userID: ${currentUser.id}")
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            Log.v("BIG", "Current loggedIn userID from preferences: $userIdInPrefs")
            val userId = if ( userIdInPrefs == currentUser.id ) currentUser.id else userIdInPrefs
            Log.i(TAG, "Fetching rooms from cache....")
            val cachedRoomsFlow = cache.roomsDao().getAllRooms(userId ?: currentUser.id)
                .map { localRooms ->
                    Log.v(TAG, "Found ${localRooms.size} rooms in cache!")
                    if(localRooms.isEmpty()) {
                        try {
                            Log.i(TAG, "Fetching rooms from Supabase....")
                            val remoteRooms = supabaseClient.from(ROOMS).select{
                                filter { eq("user_id", userId ?: currentUser.id) }
                            }.decodeList<RoomDto>()

                            if (remoteRooms.isNotEmpty()) {
                                Log.i(TAG, "Caching rooms from Supabase....")
                                cache.roomsDao().upsertRooms(remoteRooms.map { it.toRoomEntity() })
                                Log.i(TAG, "Rooms cached successfully!")
                            } else {
                                Log.i(TAG, "Nothing inside supabase!")
                            }
                        } catch (err: Throwable) {
                            Log.e(TAG, "Error fetching remote rooms: ${err.message}")
                        }
                    }
                    Resource.Success(localRooms.map { it.toRoom() })
                }
            emitAll(cachedRoomsFlow)
        } else {
            emit(Resource.Error("User not logged in!"))
        }

    }.flowOn(Dispatchers.IO)

    override fun getAvailableRooms(): Flow<Resource<List<Room>>>  = flow {
        if ( currentUser != null ) {
            Log.i(TAG, "Fetching available rooms from cache....")
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            val userId = if ( userIdInPrefs == currentUser.id ) currentUser.id else userIdInPrefs
            val cachedAvailableRoomsFlow = cache.roomsDao().getAvailableRooms(userId ?: currentUser.id)
                .map { cachedAvailableRooms ->
                Log.v(TAG, "Rooms available are ${cachedAvailableRooms.size}" )
                if ( cachedAvailableRooms.isEmpty() ) {
                    try {
                        Log.i(TAG, "Fetching available rooms if any from Supabase....")
                        val remoteAvailableRooms = supabaseClient.from(ROOMS).select{
                            filter {
                                eq("is_occupied", false)
                                eq("user_id", userId ?: currentUser.id)
                            }
                        }.decodeList<RoomDto>()

                        if (remoteAvailableRooms.isNotEmpty()) {
                            Log.i(TAG, "Caching rooms from Supabase....")
                            cache.roomsDao().upsertRooms(remoteAvailableRooms.map { it.toRoomEntity() })
                            Log.i(TAG, "Rooms cached successfully!")
                        }
                    } catch (err: Throwable) {
                        Log.e(TAG, "Error fetching remote rooms: ${err.message}")
                        Resource.Error("${err.message}")
                    }
                }
                Resource.Success(cachedAvailableRooms.map { it.toRoom() })
            }
            emitAll(cachedAvailableRoomsFlow)
        } else {
            Log.i(TAG, "User not logged in!")
            emit(Resource.Error("User not logged in!"))
        }
    }

    override fun getRoomById(roomId: String): Flow<Resource<Room?>> = flow {
        if ( currentUser != null ) {
            // Fetch room from local cache
            val localRoom = cache.roomsDao().getRoomById(roomId).first()?.toRoom()
            if (localRoom != null) {
                emit(Resource.Success(localRoom))
            } else {
                // Fetch room from remote and update the cache
                val remoteRoom = supabaseClient.from(ROOMS).select {
                    filter {
                        eq(COLUMN_ID, roomId)
                        eq("user_id", currentUser.id)
                    }
                }.decodeSingleOrNull<RoomDto>()

                remoteRoom?.let {
                    cache.roomsDao().insertRoom(it.toRoomEntity())
                    emit(Resource.Success(it.toRoom()))
                } ?: emit(Resource.Success(null))
            }
        } else {
            Log.i(TAG, "User not logged in!")
            emit(Resource.Error("User not logged in!"))
        }

    }.flowOn(Dispatchers.IO)
        .catch { e -> Resource.Error(e.message ?: "Error fetching room by id!") }

}
