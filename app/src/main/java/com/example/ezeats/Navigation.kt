package com.example.ezeats

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ezeats.Screens.AccountScreen
import com.example.ezeats.Screens.BookMarkScreen
import com.example.ezeats.Screens.HomeScreen
import com.example.ezeats.Screens.SearchScreen


sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Search : Screen("search", "Search")
    object Bookmarked : Screen("bookmarked", "Bookmarked")
    object Account : Screen("account", "Account")
}


@Composable
fun MainScreen() {
    // Create NavController for navigation
    val navController = rememberNavController()

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute != Screen.Home.route) { // Compare the route string here
                BottomNavigationBar(navController = navController)
            }
        }
    ) {innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        )
        {
            composable(
                Screen.Home.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { slideOutVertically(targetOffsetY = { -it }) }
            ) {
                HomeScreen(navController)
            }

            composable(Screen.Search.route,) { SearchScreen() }
            composable(Screen.Bookmarked.route) { BookMarkScreen() }
            composable(Screen.Account.route) { AccountScreen() }
        }
    }

}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Search, Screen.Bookmarked, Screen.Account)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentDestination?.route == screen.route,
                onClick = {
                    if (currentDestination?.route != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                label = { Text(screen.label) },
                icon = { /* Optional: Add icons here */ }
            )
        }
    }
}