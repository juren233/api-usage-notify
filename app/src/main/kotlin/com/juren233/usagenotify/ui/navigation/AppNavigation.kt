package com.juren233.usagenotify.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juren233.usagenotify.ui.screen.AddEditSiteScreen
import com.juren233.usagenotify.ui.screen.DashboardScreen
import com.juren233.usagenotify.ui.screen.SettingsScreen
import com.juren233.usagenotify.ui.screen.SiteDetailScreen

enum class Tab {
    DASHBOARD,
    SETTINGS
}

object Routes {
    const val MAIN = "main"
    const val ADD_SITE = "add_site"
    const val EDIT_SITE = "edit_site/{siteId}"
    const val SITE_DETAIL = "site_detail/{siteId}"

    fun editSite(siteId: Long) = "edit_site/$siteId"
    fun siteDetail(siteId: Long) = "site_detail/$siteId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            var currentTab by remember { mutableStateOf(Tab.DASHBOARD) }
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentTab == Tab.DASHBOARD,
                            onClick = { currentTab = Tab.DASHBOARD },
                            icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                            label = { Text("首页") }
                        )
                        NavigationBarItem(
                            selected = currentTab == Tab.SETTINGS,
                            onClick = { currentTab = Tab.SETTINGS },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                            label = { Text("设置") }
                        )
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                    when (currentTab) {
                        Tab.DASHBOARD -> DashboardScreen(
                            onNavigateToAddSite = { navController.navigate(Routes.ADD_SITE) },
                            onNavigateToSiteDetail = { navController.navigate(Routes.siteDetail(it)) },
                        )
                        Tab.SETTINGS -> SettingsScreen(
                            onNavigateBack = null
                        )
                    }
                }
            }
        }

        composable(Routes.ADD_SITE) {
            AddEditSiteScreen(
                siteId = null,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            Routes.EDIT_SITE,
            arguments = listOf(navArgument("siteId") { type = NavType.LongType }),
        ) { backStackEntry ->
            AddEditSiteScreen(
                siteId = backStackEntry.arguments?.getLong("siteId"),
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            Routes.SITE_DETAIL,
            arguments = listOf(navArgument("siteId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getLong("siteId") ?: return@composable
            SiteDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(Routes.editSite(it)) },
            )
        }
    }
}
