package com.engineerfred.easyrent.data.repository

import android.util.Log
import com.engineerfred.easyrent.constants.Constants.ROOMS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toRoom
import com.engineerfred.easyrent.data.mappers.toRoomDTO
import com.engineerfred.easyrent.data.mappers.toRoomEntity
import com.engineerfred.easyrent.data.remote.dto.RoomDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.domain.repository.RoomsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
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

    override suspend fun insertRoom(room: Room): Resource<String>  {
        try {
            Log.i(TAG, "Inserting room in cache....")

            val userId = prefs.getUserId().firstOrNull()

            if( userId == null  ) {
                Log.e(TAG, "User ID is null!")
                return Resource.Error("Not Logged In!")
            }

            val existingRoom = cache.roomsDao().getRoomByNumber(room.roomNumber)

            if ( existingRoom != null ) {
                Log.e(TAG, "Room with the same number already exists.")
                return Resource.Error("Room with this number already exists.")
            }

            cache.roomsDao().insertRoom(room.copy(userId = userId).toRoomEntity())

            val supabaseRoom = room.copy(isSynced = true, userId = userId).toRoomDTO()
            val result = supabaseClient.from(ROOMS).insert(supabaseRoom){
                select()
            }.decodeSingleOrNull<RoomDto>()

            if ( result == null ) {
                Log.e(TAG, "Room insertion failed in Supabase.")
                return Resource.Error("Failed to insert room in database")
            }

            Log.i(TAG, "Room inserted in Supabase successfully.")
            cache.roomsDao().updateRoom(result.toRoomEntity())

            Log.i(TAG, "Room insertion completed successfully and (cached).")
            return Resource.Success(room.id)

        }catch (ex: Exception) {
            Log.e(TAG, "Error inserting room: ${ex.message}")
            return Resource.Error("Error inserting room: ${ex.message}")
        }
    }

    override suspend fun deleteRoom(room: Room): Resource<Unit> {
        try {
            val userId = prefs.getUserId().firstOrNull()

            if( userId == null ) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            if ( userId != room.userId ) {
                Log.i(TAG, "Can't delete room || not owner")
                return Resource.Error("Can't delete room!")
            }

            Log.i(TAG, "Marking room as deleted in cache...")
            cache.roomsDao().markRoomAsDeleted(room.id)

            Log.i(TAG, "Successfully marked room as deleted in cache! Deleting from room from supabase....")
            val result = supabaseClient.from(ROOMS).delete {
                select()
                filter {
                    eq(COLUMN_ID, room.id)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<RoomDto>()

            if( result == null ) {
                Log.i(TAG, "Failed to delete room from supabase")
                return Resource.Success(Unit)
            }

            Log.i(TAG, "Room deleted successfully from cloud! Now permanently deleting room from cache...")
            cache.roomsDao().deleteRoom(room.id)

            Log.i(TAG, "Permanently deleted room from cache!")
            return Resource.Success(Unit)

        }catch (ex: Exception){
            Log.e(TAG, "Error deleting room: ${ex.message}")
            return Resource.Error("Error deleting room: ${ex.message}")
        }
    }

    override fun getAllRooms(): Flow<Resource<List<Room>>> = flow {

        val userId = prefs.getUserId().firstOrNull() ?: run {
            emit(Resource.Error("User not logged in!"))
            return@flow
        }

        Log.i(TAG, "Fetching rooms from cache for user: $userId")

        val cachedRoomsFlow = cache.roomsDao().getAllRooms(userId)
            .map { localRooms ->
                Log.v(TAG, "Found ${localRooms.size} rooms in cache for user: $userId")
                if(localRooms.isEmpty()) {
                    try {
                        Log.i(TAG, "Fetching rooms from Supabase for user: $userId")

                        val remoteRooms = supabaseClient.from(ROOMS).select{
                            filter { eq("user_id", userId) }
                        }.decodeList<RoomDto>()

                        if (remoteRooms.isNotEmpty()) {
                            Log.i(TAG, "Caching ${remoteRooms.size} rooms from Supabase for user: $userId")

                            cache.roomsDao().upsertRooms(remoteRooms.map { it.toRoomEntity() })
                            Log.i(TAG, "Rooms cached successfully for user: $userId")
                        }

                    } catch (err: Throwable) {
                        Log.e(TAG, "Error fetching remote rooms for user: $userId: ${err.message}")
                    }
                }
                Resource.Success(localRooms.map { it.toRoom() })
            }
        emitAll(cachedRoomsFlow)

    }.flowOn(Dispatchers.IO).catch {
        e -> emit(Resource.Error(e.message ?: "Error fetching rooms!"))
    }.distinctUntilChanged()

    override fun getAvailableRooms(): Flow<Resource<List<Room>>>  = flow {

        val userId = prefs.getUserId().firstOrNull() ?: run {
            emit(Resource.Error("User not logged in!"))
            return@flow
        }

        Log.i(TAG, "Fetching available rooms from cache for user: $userId")

        val cachedAvailableRoomsFlow = cache.roomsDao().getAvailableRooms(userId)
            .map { cachedAvailableRooms ->
                Log.v(TAG, "Rooms available are ${cachedAvailableRooms.size}" )
                if ( cachedAvailableRooms.isEmpty() ) {
                    try {
                        Log.i(TAG, "Fetching available rooms if any from Supabase....")
                        val remoteAvailableRooms = supabaseClient.from(ROOMS).select{
                            filter {
                                eq("is_occupied", false)
                                eq("user_id", userId)
                            }
                        }.decodeList<RoomDto>()

                        if (remoteAvailableRooms.isNotEmpty()) {
                            Log.i(TAG, "Caching rooms from Supabase....")

                            cache.roomsDao().upsertRooms(remoteAvailableRooms.map { it.toRoomEntity() })
                            Log.i(TAG, "Rooms cached successfully!")
                        }
                    } catch (err: Throwable) {
                        Log.e(TAG, "Error fetching remote available rooms for user: $userId: ${err.message}")
                    }
                }
                Resource.Success(cachedAvailableRooms.map { it.toRoom() })
            }
        emitAll(cachedAvailableRoomsFlow)
    }.flowOn(Dispatchers.IO).catch {
        e -> emit(Resource.Error(e.message ?: "Error fetching available rooms!"))
    }.distinctUntilChanged()

    override fun getRoomById(roomId: String): Flow<Resource<Room?>> = flow {

        val userId = prefs.getUserId().firstOrNull() ?: run {
            emit(Resource.Error("User not logged in!"))
            return@flow
        }

        val localRoom = cache.roomsDao().getRoomById(roomId).first()?.toRoom()

        if ( localRoom == null ) {
            val remoteRoom = supabaseClient.from(ROOMS).select {
                filter {
                    eq(COLUMN_ID, roomId)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<RoomDto>()

            if ( remoteRoom != null ) {
                Log.i(TAG, "Caching room from Supabase....")
                cache.roomsDao().insertRoom(remoteRoom.toRoomEntity())
                Log.i(TAG, "Room cached successfully!")
                emit(Resource.Success(remoteRoom.toRoom()))
                return@flow
            } else {
                Log.i(TAG, "Room not found in Supabase for roomId: $roomId and user: $userId")
                emit(Resource.Success(null))
                return@flow
            }
        }

        emit(Resource.Success(localRoom))

    }.flowOn(Dispatchers.IO)
        .catch { e -> emit(Resource.Error(e.message ?: "Error fetching room by id!") )}

}
