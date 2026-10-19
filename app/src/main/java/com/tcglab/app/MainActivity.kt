package com.tcglab.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tcglab.app.ui.navigation.LabRoot
import com.tcglab.app.ui.theme.LabTheme
import com.tcglab.app.ui.theme.TcgLabTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale.forLanguageTag("es")
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TcgLabApplication
        setContent {
            val themeMode by app.themePreferences.mode.collectAsState()
            TcgLabTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LabTheme.colors.background,
                ) {
                    LabRoot(
                        repository = app.repository,
                        apiKeyStore = app.apiKeyStore,
                        themePreferences = app.themePreferences,
                    )
                }
            }
        }
    }
}
