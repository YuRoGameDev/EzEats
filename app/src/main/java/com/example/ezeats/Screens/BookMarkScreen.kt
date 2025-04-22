package com.example.ezeats.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ezeats.DatabaseProvider
import com.example.ezeats.recipe.RecipePreview
import com.example.ezeats.recipe.RecipePreviewCard
import com.example.ezeats.recipe.RecipeWebView
import com.example.ezeats.recipe.fetchRecipePreviews
import kotlinx.coroutines.launch


@Composable
fun BookMarkScreen() {
    BackHandler(enabled = true) {}
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<RecipePreview>>(emptyList()) }
    var selectedRecipe by remember { mutableStateOf<RecipePreview?>(null) }
    var bookmarkedUrls = remember { mutableStateOf(DatabaseProvider.getBookmarkedUrls()) }



    print(bookmarkedUrls)
    LaunchedEffect(bookmarkedUrls.value) {
        coroutineScope.launch {
            isLoading = true
            recipes = emptyList()

            // Fetch the previews for each recipe URL
            recipes = fetchRecipePreviews(bookmarkedUrls.value)
            isLoading = false
        }
    }

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

            Button(onClick = {
                val newBookmark = "https://www.halfbakedharvest.com/giant-chocolate-chip-cookie-cookie-dough-peanut-butter-cups/"
                // Call addBookmark to add a new URL
                DatabaseProvider.addBookmark(newBookmark)

            }) {
                Text("Add Bookmark")
            }
            Button(onClick = {
                val bookmarkToRemove = "https://www.halfbakedharvest.com/giant-chocolate-chip-cookie-cookie-dough-peanut-butter-cups/"
                DatabaseProvider.removeBookmark(bookmarkToRemove)

            }) {
                Text("Remove Bookmark")
            }
        }
    }


}
