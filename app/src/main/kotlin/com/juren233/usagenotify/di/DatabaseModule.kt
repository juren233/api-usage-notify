package com.juren233.usagenotify.di

import android.content.Context
import androidx.room.Room
import com.juren233.usagenotify.data.local.AppDatabase
import com.juren233.usagenotify.data.local.BalanceRecordDao
import com.juren233.usagenotify.data.local.SiteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "usage_notify.db").build()

    @Provides
    fun provideSiteDao(db: AppDatabase): SiteDao = db.siteDao()

    @Provides
    fun provideBalanceRecordDao(db: AppDatabase): BalanceRecordDao = db.balanceRecordDao()
}
