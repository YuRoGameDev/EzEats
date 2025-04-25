package com.example.ezeats.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
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
import com.example.ezeats.recipe.RecipeFilter
import com.example.ezeats.recipe.RecipePreview
import com.example.ezeats.recipe.RecipePreviewCard
import com.example.ezeats.recipe.RecipeWebView
import com.example.ezeats.recipe.fetchRecipePreviews
import com.example.ezeats.recipe.filterAndSortRecipes
import kotlinx.coroutines.launch
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookMarkScreen() {
    BackHandler(enabled = true) {}
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<RecipePreview>>(emptyList()) }
    var selectedRecipe by remember { mutableStateOf<RecipePreview?>(null) }
    var bookmarkedUrls by remember { mutableStateOf(DatabaseProvider.getBookmarkedUrls()) }
    var refreshTrigger by remember {  mutableStateOf(0) }
    var activeFilters by remember { mutableStateOf(setOf<RecipeFilter>()) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredRecipes = filterAndSortRecipes(recipes, activeFilters)
    val refreshing = remember { mutableStateOf(false) }

    print(bookmarkedUrls)
    LaunchedEffect(refreshTrigger) {
        coroutineScope.launch {
            isLoading = true
            recipes = emptyList()

            // Fetch the previews for each recipe URL
            recipes = fetchRecipePreviews(bookmarkedUrls)
            isLoading = false
            refreshing.value = true
        }
    }

    // Display RecipeWebView when a recipe is selected
    if (selectedRecipe != null) {
        RecipeWebView(
            url = selectedRecipe!!.url,
            onBack = { selectedRecipe = null }
        )
    } else {
        val refreshState = rememberPullRefreshState(
            refreshing = refreshing.value,
            onRefresh = {
                // When refreshing, trigger the refresh logic
                refreshing.value = true
                refreshTrigger++  // This triggers the LaunchedEffect to re-fetch the data
            },
            refreshThreshold = 0.5.dp // The threshold for triggering the refresh when pulling (adjustable)
        )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .pullRefresh(refreshState)
            ) {

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(RecipeFilter.entries.toTypedArray()) { filter ->
                        FilterChip(
                            selected = activeFilters.contains(filter),
                            onClick = {
                                activeFilters = if (filter in activeFilters)
                                    activeFilters - filter
                                else
                                    activeFilters + filter
                            },
                            label = { Text(filter.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Filter by Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                val searchedRecipes =
                    filteredRecipes.filter { it.title.contains(searchQuery, ignoreCase = true) }

                when {
                    isLoading -> CircularProgressIndicator()
                    searchedRecipes.isNotEmpty() -> {
                        LazyColumn {
                            items(searchedRecipes) { recipe ->
                                RecipePreviewCard(
                                    recipe,
                                    onViewClicked = { selectedRecipe = it },
                                    onBookmarkClicked = {
                                        if (DatabaseProvider.isBookmarked(it)) {
                                            DatabaseProvider.removeBookmark(it)
                                        } else {
                                            DatabaseProvider.addBookmark(it)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                /*
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
        */

        }
    }

}
