package com.tcglab.app.data

import android.content.Context
import com.tcglab.app.BuildConfig

class ApiKeyStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("tcg_lab_secure", Context.MODE_PRIVATE)

    fun getKey(): String {
        val saved = prefs.getString(KEY, "").orEmpty().trim()
        if (saved.isNotBlank()) return saved
        return BuildConfig.XAI_API_KEY.trim()
    }

    fun hasKey(): Boolean = getKey().isNotBlank()

    fun saveKey(value: String) {
        prefs.edit().putString(KEY, value.trim()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "xai_api_key"
    }
}
