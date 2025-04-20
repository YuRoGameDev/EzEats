package com.example.ezeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.compose.*
import com.example.ezeats.ui.theme.EzEatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EzEatsTheme {
                // Create NavController for navigation
                val navController = rememberNavController()

                // Define NavHost to manage screen transitions
                NavHost(navController = navController, startDestination = "home") {
                    composable(
                        "home",
                        enterTransition = { slideInVertically(initialOffsetY = { it }) },
                        exitTransition = { slideOutVertically(targetOffsetY = { -it }) }
                    ) {
                        HomeScreen(navController)
                    }

                    composable(
                        "search" )
                    {
                        SearchScreen()
                    }

                    composable(
                        "bookmark" )
                    {
                        SearchScreen()
                    }

                    composable(
                        "account" )
                    {
                        SearchScreen()
                    }
                }
            }
        }
    }
}
