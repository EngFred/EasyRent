package com.engineerfred.easyrent.domain.usecases.rooms

import com.engineerfred.easyrent.domain.repository.RoomsRepository
import javax.inject.Inject

class GetUnsyncedRoomsUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) {
    suspend operator fun invoke() = roomsRepository.getUnsyncedRooms()
}