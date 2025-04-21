package com.example.ezeats.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ezeats.recipe.RecipePreview
import com.example.ezeats.recipe.RecipePreviewCard
import com.example.ezeats.recipe.RecipeWebView



@Composable
fun BookMarkScreen() {
    BackHandler(enabled = true) {}
//https://www.halfbakedharvest.com/giant-chocolate-chip-cookie-cookie-dough-peanut-butter-cups/
    var isLoading by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<RecipePreview>>(emptyList()) }
    var selectedRecipe by remember { mutableStateOf<RecipePreview?>(null) }

    // Display RecipeWebView when a recipe is selected
    if (selectedRecipe != null) {
        RecipeWebView(
            url = selectedRecipe!!.url,
            onBack = { selectedRecipe = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            when {
                isLoading -> CircularProgressIndicator()
                recipes.isNotEmpty() -> {
                    LazyColumn {
                        items(recipes) { recipe ->
                            RecipePreviewCard(recipe, onViewClicked = {selectedRecipe = it})
                        }
                    }
                }
            }
        }
    }
}
