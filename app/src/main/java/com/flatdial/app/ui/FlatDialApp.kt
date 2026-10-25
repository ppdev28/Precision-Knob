package com.flatdial.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flatdial.app.R
import com.flatdial.app.ui.equalizer.EqualizerScreen
import com.flatdial.app.ui.equalizer.ProfileSearchScreen
import com.flatdial.app.ui.settings.SettingsScreen
import com.flatdial.app.ui.settings.SettingsViewModel
import com.flatdial.app.ui.theme.FlatDialTheme
import com.flatdial.app.ui.volume.VolumeScreen

private enum class Destination(val route: String, val label: Int, val icon: ImageVector?) {
    Volume("volume", R.string.nav_volume, Icons.AutoMirrored.Filled.VolumeUp),
    Equalizer("equalizer", R.string.nav_equalizer, Icons.Filled.Tune),
    Profiles("profiles", R.string.search_title, null),
    Settings("settings", R.string.nav_settings, Icons.Filled.Settings),
}

@Composable
fun FlatDialApp() {
    // Activity-scoped so theme and dial preferences stay in sync across destinations.
    val settingsVm: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val settings by settingsVm.settings.collectAsStateWithLifecycle()

    FlatDialTheme(themeMode = settings.themeMode) {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentDestination = backStack?.destination
        val bottomDestinations = listOf(Destination.Volume, Destination.Equalizer, Destination.Settings)
        val showBottomBar = currentDestination?.route in bottomDestinations.map { it.route }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                        bottomDestinations.forEach { dest ->
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(dest.icon!!, contentDescription = null) },
                                label = { Text(stringResource(dest.label)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Destination.Volume.route,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                composable(Destination.Volume.route) {
                    VolumeScreen(settings = settings)
                }
                composable(Destination.Equalizer.route) {
                    EqualizerScreen(onFindProfile = { navController.navigate(Destination.Profiles.route) })
                }
                composable(Destination.Profiles.route) { ProfileSearchScreen(onBack = { navController.popBackStack() }) }
                composable(Destination.Settings.route) { SettingsScreen() }
            }
        }
    }
}
