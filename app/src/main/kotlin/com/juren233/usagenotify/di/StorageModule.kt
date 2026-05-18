package com.juren233.usagenotify.di

import android.content.Context
import com.juren233.usagenotify.data.local.CredentialStore
import com.juren233.usagenotify.data.local.SettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideCredentialStore(@ApplicationContext context: Context): CredentialStore =
        CredentialStore.fromContext(context)

    @Provides
    @Singleton
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore =
        SettingsStore(context)
}
