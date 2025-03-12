package com.engineerfred.easyrent.domain.usecases.rooms

import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.repository.RoomsRepository
import javax.inject.Inject

class DeleteRoomUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) {
    suspend operator fun invoke(room: Room) = roomsRepository.deleteRoom(room)
}