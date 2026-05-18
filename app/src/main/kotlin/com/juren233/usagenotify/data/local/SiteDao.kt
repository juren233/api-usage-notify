package com.juren233.usagenotify.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juren233.usagenotify.data.model.Site
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Site>>

    @Query("SELECT * FROM sites WHERE isEnabled = 1")
    suspend fun getEnabled(): List<Site>

    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getById(id: Long): Site?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(site: Site): Long

    @Delete
    suspend fun delete(site: Site)
}
