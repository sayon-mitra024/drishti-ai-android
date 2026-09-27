package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreeningDao {

    @Query("SELECT * FROM screenings ORDER BY timestamp DESC")
    fun getAllScreenings(): Flow<List<ScreeningEntity>>

    @Query("SELECT * FROM screenings WHERE id = :id")
    suspend fun getScreeningById(id: Long): ScreeningEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreening(screening: ScreeningEntity): Long

    @Update
    suspend fun updateScreening(screening: ScreeningEntity)

    @Query("DELETE FROM screenings WHERE id = :id")
    suspend fun deleteScreeningById(id: Long)

    @Query("SELECT COUNT(*) FROM screenings")
    fun getScreeningCount(): Flow<Int>
}
