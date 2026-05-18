package com.juren233.usagenotify.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CredentialStore private constructor(
    private val prefs: SharedPreferences,
) {
    fun save(ref: String, token: String) {
        prefs.edit().putString(ref, token).apply()
    }

    fun load(ref: String): String? = prefs.getString(ref, null)

    fun delete(ref: String) {
        prefs.edit().remove(ref).apply()
    }

    companion object {
        private const val PREFS_NAME = "usage_notify_credentials"

        fun fromContext(context: Context): CredentialStore {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            return CredentialStore(prefs)
        }
    }
}
