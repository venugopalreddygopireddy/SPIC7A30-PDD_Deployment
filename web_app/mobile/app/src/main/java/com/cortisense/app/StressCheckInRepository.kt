package com.cortisense.app

import kotlinx.coroutines.flow.Flow

class StressCheckInRepository(private val dao: StressCheckInDao) {
    val allCheckIns: Flow<List<StressCheckInEntity>> = dao.getAllCheckIns()

    suspend fun insert(checkIn: StressCheckInEntity) {
        dao.insertCheckIn(checkIn)
    }

    suspend fun getById(id: Long): StressCheckInEntity? {
        return dao.getCheckInById(id)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
