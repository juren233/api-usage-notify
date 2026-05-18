package com.juren233.usagenotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sites")
data class Site(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val type: SiteType,
    val credentialRef: String,
    val isEnabled: Boolean = true,
    val alertThreshold: Double = 1.0,
    val createdAt: Long = System.currentTimeMillis(),
)
