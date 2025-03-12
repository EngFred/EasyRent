package com.engineerfred.easyrent.domain.usecases.rooms

import com.engineerfred.easyrent.domain.repository.RoomsRepository
import javax.inject.Inject

class GetAvailableRoomsUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) {
    operator fun invoke() = roomsRepository.getAvailableRooms()
}