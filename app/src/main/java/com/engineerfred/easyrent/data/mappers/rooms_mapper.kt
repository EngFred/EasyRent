package com.engineerfred.easyrent.data.mappers

import com.engineerfred.easyrent.data.local.entity.RoomEntity
import com.engineerfred.easyrent.data.remote.dto.RoomDto
import com.engineerfred.easyrent.domain.modals.Room

fun RoomDto.toRoomEntity() = RoomEntity(
    id = id,
    roomType = roomType,
    monthlyRent = monthlyRent,
    isOccupied = isOccupied,
    isSynced = isSynced,
    roomNumber = roomNumber,
    isDeleted = isDeleted,
    userId = userId
)

fun RoomEntity.toRoom() = Room(
    id = id,
    roomType = roomType,
    monthlyRent = monthlyRent,
    isOccupied = isOccupied,
    isSynced = isSynced,
    roomNumber = roomNumber,
    isDeleted = isDeleted,
    userId = userId
)

fun RoomEntity.toRoomDto() = RoomDto(
    id = id,
    roomType = roomType,
    monthlyRent = monthlyRent,
    isOccupied = isOccupied,
    isSynced = isSynced,
    roomNumber = roomNumber,
    isDeleted = isDeleted,
    userId = userId
)

fun Room.toRoomEntity() = RoomEntity(
    id = id,
    roomType = roomType,
    monthlyRent = monthlyRent,
    isOccupied = isOccupied,
    isSynced = isSynced,
    roomNumber = roomNumber,
    isDeleted = isDeleted,
    userId = userId
)

fun Room.toRoomDTO() = RoomDto(
    id = id,
    roomType = roomType,
    roomNumber = roomNumber,
    monthlyRent = monthlyRent,
    isOccupied = isOccupied,
    isDeleted = isDeleted,
    isSynced = isSynced,
    userId = userId
)

fun RoomDto.toRoom() = Room(
    id = id,
    roomType = roomType,
    monthlyRent = monthlyRent,
    isOccupied = isOccupied,
    isSynced = isSynced,
    roomNumber = roomNumber,
    isDeleted = isDeleted,
    userId = userId
)