package com.tcglab.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.tcglab.app.ui.decks.DecksViewModel
import com.tcglab.app.ui.grok.GrokViewModel
import com.tcglab.app.ui.home.HomeScreen
import com.tcglab.app.ui.settings.SettingsScreen
import com.tcglab.app.ui.strategy.StrategyScreen
import com.tcglab.app.ui.theme.LabTheme

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
    val navController = rememberNavController()
    val collectionVm: CollectionViewModel = viewModel(factory = CollectionViewModel.factory(repository))
    val decksVm: DecksViewModel = viewModel(factory = DecksViewModel.factory(repository))
    val grokVm: GrokViewModel = viewModel(factory = GrokViewModel.factory(repository, apiKeyStore))
    val colors = LabTheme.colors

    Scaffold(
        containerColor = colors.background,
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
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
                DeckBuilderScreen(
                    decksVm = decksVm,
                    onBack = { navController.popBackStack() },
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
