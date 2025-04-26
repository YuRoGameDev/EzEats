package com.example.ezeats

import android.content.res.Configuration
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF9dc484)) // your desired green
            .systemBarsPadding()           // accounts for status & nav bar
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentRoute != Screen.Home.route) { // Compare the route string here
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

}

@Composable
fun BottomNavigationBar(navController: NavHostController, isLandscape: Boolean) {
    val items = listOf(Screen.Search, Screen.Bookmarked, Screen.Account)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF9dc484),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLandscape) 65.dp else 80.dp) // Compact in landscape
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                modifier = Modifier
                    .systemBarsPadding()
                    .size(70.dp)
                    .offset(y = 10.dp),
                selected = currentRoute == screen.route,
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
                icon = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(id = when (screen.route) {
                                Screen.Search.route -> R.drawable.search
                                Screen.Bookmarked.route -> R.drawable.bookmarked
                                else -> R.drawable.useraccount
                            }),
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