package com.example.ezeats.Screens

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.ezeats.DatabaseProvider
import com.example.ezeats.Screen
import com.example.ezeats.R

@Composable
fun HomeScreen(navController: NavController) {


    BackHandler(enabled = true) {
        // Do nothing, back is disabled
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val buttonMaxWidth = if (screenWidth > 600.dp) 400.dp else Dp.Unspecified

    val isLoggedIn = remember { mutableStateOf(false) }
    LaunchedEffect(true) {
        val status = DatabaseProvider.db.userDataDao().isUserLoggedIn()
        isLoggedIn.value = status
        println("Is Logged in?:" + status)
        //DatabaseProvider.db.userDataDao().updateBookmarkedUrls(listOf("https://www.halfbakedharvest.com/giant-chocolate-chip-cookie-cookie-dough-peanut-butter-cups/"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val imageSize = if (isLandscape) 150.dp else 250.dp  // adjust sizes as needed

        Image(
            painter = painterResource(id = R.drawable.logo_transparent),
            contentDescription = "Logo",
            modifier = Modifier.size(imageSize)
        )

        /*
        Text(
            text = "EZ-Eats",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 15.dp),
            fontSize = 80.sp
        )
        */
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Screen.Search.route) },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7cac54)),
            modifier = Modifier
                .then(if (buttonMaxWidth != Dp.Unspecified) Modifier.width(buttonMaxWidth) else Modifier.fillMaxWidth())
                .height(if (isLandscape) 50.dp else 64.dp )
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

        BoxWithConstraints {
            val isLandscape = this.maxWidth > 600.dp  // use 'this' or omit since it's in scope

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
                            .then(if (buttonMaxWidth != Dp.Unspecified) Modifier.width(buttonMaxWidth) else Modifier.fillMaxWidth())
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
                            .then(if (buttonMaxWidth != Dp.Unspecified) Modifier.width(buttonMaxWidth) else Modifier.fillMaxWidth())
                            .height(52.dp)
                    ) {
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
