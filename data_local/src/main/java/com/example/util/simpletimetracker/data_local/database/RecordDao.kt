package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.util.simpletimetracker.data_local.model.RecordDBO
import com.example.util.simpletimetracker.data_local.model.RecordWithRecordTagsDBO

@Dao
interface RecordDao {

    @Transaction
    @Query("SELECT * FROM records")
    suspend fun getAll(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds)")
    suspend fun getByType(typesIds: List<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started < :end AND time_ended > :start")
    suspend fun getFromRange(start: Long, end: Long): List<RecordWithRecordTagsDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordDBO): Long

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM records WHERE type_id = :typeId")
    suspend fun deleteByType(typeId: Long)

    @Query("DELETE FROM records")
    suspend fun clear()
}