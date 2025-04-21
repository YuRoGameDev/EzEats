package com.example.ezeats

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ezeats.recipe.RecipePreview
import com.example.ezeats.recipe.RecipePreviewCard
import com.example.ezeats.recipe.RecipeWebView
import com.example.ezeats.recipe.fetchRecipePreview
import kotlinx.coroutines.launch


@Composable
fun SearchScreen() {
    BackHandler(enabled = true) {
        // Do nothing, back is disabled
    }

    val coroutineScope = rememberCoroutineScope()
    var inputUrl by remember { mutableStateOf("") }
    var recipe by remember { mutableStateOf<RecipePreview?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showWebView by remember { mutableStateOf(false) }

    if (showWebView && recipe != null) {
        RecipeWebView(
            url = recipe!!.url,
            onBack = { showWebView = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("Paste Recipe URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        recipe = fetchRecipePreview(inputUrl)
                        isLoading = false
                    }
                },
                enabled = inputUrl.isNotBlank()
            ) {
                Text("Fetch Recipe")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> CircularProgressIndicator()
                recipe != null -> {
                    RecipePreviewCard(recipe!!)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showWebView = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Full Recipe")
                    }
                }
            }
        }
    }
}




