package com.example.ezeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.ui.theme.EzEatsTheme

//Creates the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Sets up the Offline Data
        DatabaseProvider.setList()

        setContent {
            EzEatsTheme {
                MainScreen()
            }
        }
    }
}
