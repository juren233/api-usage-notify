package com.juren233.usagenotify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juren233.usagenotify.ui.screen.AddEditSiteScreen
import com.juren233.usagenotify.ui.screen.DashboardScreen
import com.juren233.usagenotify.ui.screen.SettingsScreen
import com.juren233.usagenotify.ui.screen.SiteDetailScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_SITE = "add_site"
    const val EDIT_SITE = "edit_site/{siteId}"
    const val SITE_DETAIL = "site_detail/{siteId}"
    const val SETTINGS = "settings"

    fun editSite(siteId: Long) = "edit_site/$siteId"
    fun siteDetail(siteId: Long) = "site_detail/$siteId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToAddSite = { navController.navigate(Routes.ADD_SITE) },
                onNavigateToSiteDetail = { navController.navigate(Routes.siteDetail(it)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
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

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
