package com.example.ezeats.Screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*

import androidx.navigation.NavController
import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.Screen
import com.example.ezeats.R
import com.example.ezeats.ui.theme.lightBlue

//This is the starting home screen
//It only shows on startup and isn't used again
@Composable
fun HomeScreen(navController: NavController) {
    BackHandler(enabled = true) {}
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val buttonMaxWidth = if (screenWidth > 600.dp) 400.dp else Dp.Unspecified

    val isLoggedIn = remember { mutableStateOf(false) }
    LaunchedEffect(true) {
        val status = DatabaseProvider.isLoggedIn
        isLoggedIn.value = status
        println("Is Logged in?:" + status)
    }

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val imageSize = if (isLandscape) 150.dp else 250.dp
        //Logo
        Image(
            painter = painterResource(id = R.drawable.logo_transparent),
            contentDescription = "Logo",
            modifier = Modifier.size(imageSize)
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Search Screen button
        Button(
            onClick = { navController.navigate(Screen.Search.route) },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = lightBlue),
            modifier = Modifier
                .then(if (buttonMaxWidth != Dp.Unspecified) Modifier.width(buttonMaxWidth) else Modifier.fillMaxWidth())
                .height(if (isLandscape) 50.dp else 64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search for Recipes",
                fontSize = 28.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //These are the Bookmark and account buttons
        //They each get custom composables if the user switches from portrait to landscape
        BoxWithConstraints {
            val isLandscape = this.maxWidth > 600.dp

            if (isLandscape) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Bookmarked.route) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .width(50.dp)
                            .height(52.dp)
                    ) {
                        Text("View Bookmarked Recipes", fontSize = 25.sp)
                    }

                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Account.route) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .width(50.dp)
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isLoggedIn.value) "My Account" else "Create Account",
                            fontSize = 25.sp
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Bookmarked.route) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier
                            .then(
                                if (buttonMaxWidth != Dp.Unspecified) Modifier.width(
                                    buttonMaxWidth
                                ) else Modifier.fillMaxWidth()
                            )
                            .height(52.dp)
                    ) {
                        Text("View Bookmarked Recipes", fontSize = 25.sp)
                    }

                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Account.route) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier
                            .then(
                                if (buttonMaxWidth != Dp.Unspecified) Modifier.width(
                                    buttonMaxWidth
                                ) else Modifier.fillMaxWidth()
                            )
                            .height(52.dp)
                    ) {
                        //If the user is already logged in, this text will change
                        Text(
                            text = if (isLoggedIn.value) "My Account" else "Create Account",
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }
    }
}
