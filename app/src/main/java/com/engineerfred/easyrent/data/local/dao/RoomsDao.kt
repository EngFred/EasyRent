package com.engineerfred.easyrent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.engineerfred.easyrent.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRoom(room: RoomEntity)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Query("DELETE FROM ROOMS WHERE id = :roomId")
    suspend fun deleteRoom(roomId: String)

    @Query("SELECT * FROM ROOMS WHERE isDeleted = 0 AND userId = :userId")
    fun getAllRooms(userId: String) : Flow<List<RoomEntity>>

    @Query("SELECT * FROM ROOMS WHERE id = :roomId")
    fun getRoomById(roomId: String) : Flow<RoomEntity?>

    @Query("SELECT * FROM ROOMS WHERE roomNumber = :roomNumber")  //to avoid duplicate room numbers
    suspend fun getRoomByNumber(roomNumber: Int) : RoomEntity?

    @Query("SELECT * FROM ROOMS WHERE isOccupied = 0 AND isDeleted = 0 AND userId = :userId")
    fun getAvailableRooms(userId: String): Flow<List<RoomEntity>>

    @Upsert
    suspend fun upsertRooms(remoteRooms: List<RoomEntity> )

    @Query("SELECT * FROM ROOMS WHERE isSynced = 0 AND isDeleted = 0 AND userId = :userId")
    suspend fun getUnsyncedRooms(userId: String): List<RoomEntity>

    @Query("SELECT * FROM ROOMS WHERE isDeleted = 1 AND userId = :userId")
    suspend fun getAllLocallyDeletedRooms(userId: String): List<RoomEntity>

    @Query("UPDATE ROOMS SET isOccupied = :isOccupied, isSynced = 0 WHERE id = :roomId")
    suspend fun updateRoomStatus(roomId: String, isOccupied: Boolean)

    @Query("UPDATE ROOMS SET isDeleted = 1 WHERE id = :roomId")
    suspend fun markRoomAsDeleted(roomId: String)

    //deleting everything on sign out ( clearing cache )
    @Query("DELETE FROM ROOMS")
    suspend fun deleteAllCachedRooms()

}