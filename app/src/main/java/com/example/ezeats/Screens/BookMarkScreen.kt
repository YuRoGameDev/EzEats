package com.example.ezeats.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

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
            refreshing.value = false
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
            refreshThreshold = 100.dp // The threshold for triggering the refresh when pulling (adjustable)
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .background(Color(0xFF9dc484)) // First: full-width background
                    .fillMaxWidth()
                    .padding(16.dp) // Then: inner spacing for content
            ) {
                val darkGreen = Color(0xFF49891a)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            // Limit text to 100 characters
                            if (newValue.length <= 45) {
                                searchQuery = newValue
                            }
                        },
                        label = { Text("Filter by Title") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = darkGreen,
                            unfocusedIndicatorColor = darkGreen,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black,
                            cursorColor = darkGreen,
                            disabledIndicatorColor = Color.Gray
                        ),
                        textStyle = TextStyle(
                            color = Color.Black, // Set the text color here
                            fontWeight = FontWeight.Bold, // Font weight
                            fontSize = 18.sp // Adjust the font size
                        )
                    )

                    Button(
                        onClick = {
                            refreshTrigger++
                        },
                        enabled = true,
                        modifier = Modifier.fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkGreen,  // For background color
                            contentColor = Color.Black   // For icon and text color
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(32.dp), // Adjust size as needed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                            label = { Text(filter.label) },
                            modifier = Modifier.height(50.dp), // Set the height of the chip
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent, // Background color for the chip
                                labelColor = Color.Black,   // Text color inside the chip
                                selectedContainerColor = darkGreen, // Keep the selected background color dark green
                                selectedLabelColor = Color.White

                            )
                        )
                    }
                }


            }

                Spacer(modifier = Modifier.height(16.dp))

                val searchedRecipes =
                    filteredRecipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
            Box(modifier = Modifier
                .fillMaxSize()
                .pullRefresh(refreshState)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    recipes.isEmpty() -> {
                        // Initial state
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "There's nothing here.\nTry searching for some recipes to bookmark!",
                                    color = Color.Black,
                                    fontSize = 30.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 40.sp
                                )
                            }
                        }
                    }

                    !recipes.isEmpty() && searchQuery.isNotBlank() -> {
                        // After searching, no results
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Nothing found.\nTry searching for some recipes!",
                                    color = Color.Black,
                                    fontSize = 30.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 40.sp
                                )
                            }
                        }
                    }

                    searchedRecipes.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),

                            ) {
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
            }
        }
    }

}
