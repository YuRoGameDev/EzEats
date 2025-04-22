package com.example.ezeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.example.ezeats.ui.theme.EzEatsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DatabaseProvider.setList()
        setContent {
            EzEatsTheme {
                MainScreen()
            }
        }
    }
}
