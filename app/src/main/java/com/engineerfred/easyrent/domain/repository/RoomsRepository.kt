package com.engineerfred.easyrent.domain.repository

import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Room
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {
    suspend fun insertRoom( room: Room ) : Resource<String>
    //suspend fun updateRoom(room: Room) : Resource<Unit>
    suspend fun deleteRoom(room: Room) : Resource<Unit>
    fun getAllRooms() : Flow<Resource<List<Room>>>
    fun getAvailableRooms() : Flow<Resource<List<Room>>>
    fun getRoomById(roomId: String) : Flow<Resource<Room?>>
}