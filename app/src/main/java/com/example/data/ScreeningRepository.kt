package com.example.data

import kotlinx.coroutines.flow.Flow

class ScreeningRepository(private val screeningDao: ScreeningDao) {
    val allScreenings: Flow<List<ScreeningEntity>> = screeningDao.getAllScreenings()
    val totalScreeningsCount: Flow<Int> = screeningDao.getScreeningCount()

    suspend fun getScreeningById(id: Long): ScreeningEntity? {
        return screeningDao.getScreeningById(id)
    }

    suspend fun saveScreening(screening: ScreeningEntity): Long {
        return screeningDao.insertScreening(screening)
    }

    suspend fun updateScreening(screening: ScreeningEntity) {
        screeningDao.updateScreening(screening)
    }

    suspend fun deleteScreening(id: Long) {
        screeningDao.deleteScreeningById(id)
    }
}
