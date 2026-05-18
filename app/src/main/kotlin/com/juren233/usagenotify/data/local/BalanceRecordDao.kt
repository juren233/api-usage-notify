package com.juren233.usagenotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.juren233.usagenotify.data.model.BalanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceRecordDao {
    @Insert
    suspend fun insert(record: BalanceRecord)

    @Query(
        """
        SELECT * FROM balance_records
        WHERE siteId = :siteId AND timestamp >= :since
        ORDER BY timestamp ASC
        """
    )
    fun observeForSite(siteId: Long, since: Long): Flow<List<BalanceRecord>>

    @Query("SELECT * FROM balance_records WHERE siteId = :siteId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(siteId: Long): BalanceRecord?

    @Query("DELETE FROM balance_records WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
