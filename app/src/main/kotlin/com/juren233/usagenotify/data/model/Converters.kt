package com.juren233.usagenotify.data.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSiteType(value: SiteType): String = value.name

    @TypeConverter
    fun toSiteType(value: String): SiteType = SiteType.valueOf(value)
}
