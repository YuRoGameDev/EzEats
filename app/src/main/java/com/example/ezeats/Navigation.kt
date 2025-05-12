package com.example.ezeats

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ezeats.Screens.*

//All the Navigation Routes
sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Search : Screen("search", "Search")
    object Bookmarked : Screen("bookmarked", "Bookmarked")
    object Account : Screen("account", "Account")
}

//Handles navigation. Uses Scaffolding to keep the Navigation Bar at the bottom
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF9dc484))
            .systemBarsPadding()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentRoute != Screen.Home.route) {
                    BottomNavigationBar(navController = navController, isLandscape = isLandscape)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
            )
            {
                //Start at home page, then access rest of pages
                //Home page also gets special animation transition
                composable(
                    Screen.Home.route,
                    enterTransition = { slideInVertically(initialOffsetY = { it }) },
                    exitTransition = { slideOutVertically(targetOffsetY = { -it }) }
                ) {
                    HomeScreen(navController)
                }

                composable(Screen.Search.route) { SearchScreen() }
                composable(Screen.Bookmarked.route) { BookMarkScreen() }
                composable(Screen.Account.route) { AccountScreen() }
            }
        }
    }

}

//The actual footer bar
@Composable
fun BottomNavigationBar(navController: NavHostController, isLandscape: Boolean) {
    val items = listOf(Screen.Search, Screen.Bookmarked, Screen.Account)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF9dc484),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLandscape) 65.dp else 80.dp)
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                modifier = Modifier
                    .systemBarsPadding()
                    .size(70.dp)
                    .offset(y = 10.dp),
                selected = currentRoute == screen.route,
                //Navigation clicker
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                //Individual icons
                icon = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(
                                id = when (screen.route) {
                                    Screen.Search.route -> R.drawable.search
                                    Screen.Bookmarked.route -> R.drawable.bookmarked
                                    else -> R.drawable.useraccount
                                }
                            ),
                            contentDescription = screen.label,
                            modifier = Modifier.size(if (isLandscape) 60.dp else 72.dp)
                        )
                    }
                },
                label = {},
                alwaysShowLabel = false,
                colors = NavigationBarItemColors(
                    selectedIconColor = Color(0xFFd4d4dc),
                    selectedTextColor = Color.White,
                    selectedIndicatorColor = Color(0xFFd4d4dc),
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White,
                    disabledIconColor = Color.White,
                    disabledTextColor = Color.White,
                )
            )
        }
    }
}