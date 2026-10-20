package com.tcglab.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tcglab.app.data.ApiKeyStore
import com.tcglab.app.data.LabRepository
import com.tcglab.app.data.ThemePreferences
import com.tcglab.app.ui.collection.CollectionListScreen
import com.tcglab.app.ui.collection.CollectionViewModel
import com.tcglab.app.ui.collection.SetDetailScreen
import com.tcglab.app.ui.decks.DeckBuilderScreen
import com.tcglab.app.ui.decks.DecksScreen
import com.tcglab.app.ui.decks.DecksViewModel
import com.tcglab.app.ui.grok.GrokViewModel
import com.tcglab.app.ui.home.HomeScreen
import com.tcglab.app.ui.settings.SettingsScreen
import com.tcglab.app.ui.strategy.StrategyScreen
import com.tcglab.app.ui.theme.LabTheme
import kotlinx.coroutines.launch

private object Routes {
    const val Home = "home"
    const val Collection = "collection"
    const val Set = "set/{code}"
    const val Deck = "deck"
    const val DeckId = "deck/{id}"
    const val Strategy = "strategy"
    const val Settings = "settings"
}

@Composable
fun LabRoot(
    repository: LabRepository,
    apiKeyStore: ApiKeyStore,
    themePreferences: ThemePreferences,
) {
    val catalogReady by repository.catalogReady.collectAsState()
    val catalogError by repository.catalogError.collectAsState()
    val scope = rememberCoroutineScope()
    val colors = LabTheme.colors

    Scaffold(
        containerColor = colors.background,
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        if (!catalogReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                if (catalogError == null) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("No se pudo cargar el catálogo.")
                        Text(
                            text = catalogError.orEmpty(),
                            modifier = Modifier.padding(16.dp),
                        )
                        Button(onClick = { scope.launch { repository.bootstrap() } }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            return@Scaffold
        }

        val navController = rememberNavController()
        val collectionVm: CollectionViewModel =
            viewModel(factory = CollectionViewModel.factory(repository))
        val decksVm: DecksViewModel =
            viewModel(factory = DecksViewModel.factory(repository))
        val grokVm: GrokViewModel =
            viewModel(factory = GrokViewModel.factory(repository, apiKeyStore))

        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    collectionVm = collectionVm,
                    onOpenCollection = { navController.navigate(Routes.Collection) },
                    onOpenDeck = { navController.navigate(Routes.Deck) },
                    onOpenStrategy = { navController.navigate(Routes.Strategy) },
                    onOpenSettings = { navController.navigate(Routes.Settings) },
                )
            }
            composable(Routes.Collection) {
                CollectionListScreen(
                    collectionVm = collectionVm,
                    onBack = { navController.popBackStack() },
                    onOpenSet = { code -> navController.navigate("set/$code") },
                )
            }
            composable(
                route = Routes.Set,
                arguments = listOf(navArgument("code") { type = NavType.StringType }),
            ) { entry ->
                SetDetailScreen(
                    setCode = entry.arguments?.getString("code").orEmpty(),
                    viewModel = collectionVm,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.Deck) {
                DecksScreen(
                    viewModel = decksVm,
                    onOpenDeck = { id -> navController.navigate("deck/$id") },
                    onNewDeck = { navController.navigate("strategy") },
                )
            }
            composable(
                route = Routes.DeckId,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                DeckBuilderScreen(
                    decksVm = decksVm,
                    initialDeckId = entry.arguments?.getString("id"),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.Strategy) {
                StrategyScreen(
                    grokVm = grokVm,
                    onBack = { navController.popBackStack() },
                    onOpenDeck = { id -> navController.navigate("deck/$id") },
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    themePreferences = themePreferences,
                    apiKeyStore = apiKeyStore,
                    onBack = { navController.popBackStack() },
                    onApiKeyChanged = { grokVm.refreshKeyState() },
                )
            }
        }
    }
}
