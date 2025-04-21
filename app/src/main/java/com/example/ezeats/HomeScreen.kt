package com.example.ezeats

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    BackHandler(enabled = true) {
        // Do nothing, back is disabled
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val buttonMaxWidth = if (screenWidth > 600.dp) 400.dp else Dp.Unspecified

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "EZ-Eats",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 15.dp),
            fontSize = 80.sp
        )

        Button(
            onClick = { navController.navigate(Screen.Search.route) },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier
                .then(if (buttonMaxWidth != Dp.Unspecified) Modifier.width(buttonMaxWidth) else Modifier.fillMaxWidth())
                .height(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search for Recipes",
                fontSize = 25.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate(Screen.Account.route) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            border = BorderStroke(2.dp, Color.Black),
            modifier = Modifier
                .then(if (buttonMaxWidth != Dp.Unspecified) Modifier.width(buttonMaxWidth) else Modifier.fillMaxWidth())
                .height(52.dp)
        ) {
            Text("Create Account", fontSize = 25.sp)
        }
    }
}
