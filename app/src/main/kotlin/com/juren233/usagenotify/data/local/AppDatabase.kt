package com.juren233.usagenotify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.juren233.usagenotify.data.model.BalanceRecord
import com.juren233.usagenotify.data.model.Converters
import com.juren233.usagenotify.data.model.Site

@Database(
    entities = [Site::class, BalanceRecord::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun balanceRecordDao(): BalanceRecordDao
}
