package com.tcglab.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "tcg_lab_secure",
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun getKey(): String = prefs.getString(KEY, "").orEmpty().trim()

    fun hasKey(): Boolean = getKey().isNotBlank()

    fun saveKey(value: String) {
        val normalized = value.trim()
        if (normalized.isBlank()) clear() else prefs.edit().putString(KEY, normalized).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "xai_api_key"
    }
}
