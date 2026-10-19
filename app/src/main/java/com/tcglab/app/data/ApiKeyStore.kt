package com.tcglab.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = EncryptedSharedPreferences.create(
        appContext,
        "tcg_lab_secure",
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    init {
        migrateLegacyKey()
    }

    fun getKey(): String = prefs.getString(KEY, "").orEmpty().trim()

    fun hasKey(): Boolean = getKey().isNotBlank()

    fun saveKey(value: String) {
        val normalized = value.trim()
        if (normalized.isBlank()) clear() else prefs.edit().putString(KEY, normalized).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
        appContext.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY)
            .apply()
    }

    private fun migrateLegacyKey() {
        val legacy = appContext.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        val legacyKey = legacy.getString(KEY, "").orEmpty().trim()
        if (legacyKey.isNotBlank() && getKey().isBlank()) {
            prefs.edit().putString(KEY, legacyKey).commit()
        }
        legacy.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "xai_api_key"
        private const val LEGACY_PREFS = "tcg_lab_secure"
    }
}
