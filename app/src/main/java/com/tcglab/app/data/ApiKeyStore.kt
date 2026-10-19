package com.tcglab.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.KeyStore

class ApiKeyStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(ENCRYPTED_PREFS, Context.MODE_PRIVATE)

    init {
        migrateLegacyKey()
    }

    fun getKey(): String {
        val ciphertext = prefs.getString(KEY, null) ?: return ""
        return runCatching { decrypt(ciphertext) }.getOrDefault("")
    }

    fun hasKey(): Boolean = getKey().isNotBlank()

    fun saveKey(value: String) {
        val normalized = value.trim()
        if (normalized.isBlank()) {
            clear()
        } else {
            prefs.edit().putString(KEY, encrypt(normalized)).apply()
        }
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
            saveKey(legacyKey)
        }
        legacy.edit().remove(KEY).apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encrypted
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        require(payload.size > IV_LENGTH) { "Clave cifrada inválida." }
        val iv = payload.copyOfRange(0, IV_LENGTH)
        val ciphertext = payload.copyOfRange(IV_LENGTH, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(TAG_LENGTH_BITS, iv),
        )
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey = synchronized(KEY_LOCK) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return@synchronized it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        generator.generateKey()
    }

    companion object {
        private const val KEY = "xai_api_key"
        private const val LEGACY_PREFS = "tcg_lab_secure"
        private const val ENCRYPTED_PREFS = "tcg_lab_encrypted"
        private const val KEY_ALIAS = "tcg_lab_api_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH_BITS = 128
        private val KEY_LOCK = Any()
    }
}
