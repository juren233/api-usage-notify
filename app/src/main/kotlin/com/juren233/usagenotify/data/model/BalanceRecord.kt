package com.juren233.usagenotify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "balance_records",
    foreignKeys = [ForeignKey(
        entity = Site::class,
        parentColumns = ["id"],
        childColumns = ["siteId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("siteId"), Index("timestamp")],
)
data class BalanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: Long,
    val balance: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
