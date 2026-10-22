package com.tcglab.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.tcglab.app.data.ApiKeyStore
import com.tcglab.app.data.AppLogger
import com.tcglab.app.data.LabRepository
import com.tcglab.app.data.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

class TcgLabApplication : Application() {
    lateinit var repository: LabRepository
        private set
    lateinit var apiKeyStore: ApiKeyStore
        private set
    lateinit var themePreferences: ThemePreferences
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(withSpanishLocale(base))
    }

    override fun onCreate() {
        super.onCreate()
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.error(this, "Excepción no controlada en " + thread.name, throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
        AppLogger.info(this, "Aplicación iniciada")
        apiKeyStore = ApiKeyStore(this)
        themePreferences = ThemePreferences(this)
        repository = LabRepository(this)
        appScope.launch { repository.bootstrap() }
    }

    private fun withSpanishLocale(context: Context): Context {
        val locale = Locale.forLanguageTag("es")
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
