package com.example.ezeats.Screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshState
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
import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.recipe.RecipeFilter
import com.example.ezeats.recipe.RecipePreview
import com.example.ezeats.recipe.RecipePreviewCard
import com.example.ezeats.recipe.RecipeWebView
import com.example.ezeats.recipe.fetchRecipePreviews
import com.example.ezeats.recipe.filterAndSortRecipes
import kotlinx.coroutines.launch
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                BookmarkHeaderSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onRefreshClicked = { refreshTrigger++ },
                    activeFilters = activeFilters,
                    onFilterClicked = { filter ->
                        activeFilters = if (filter in activeFilters)
                            activeFilters - filter
                        else
                            activeFilters + filter
                    },
                    modifier = Modifier
                        .background(Color(0xFF7a9c65))
                        .fillMaxHeight()
                        .width(350.dp)
                )

                BookmarkRecipeSection(
                    isLoading = isLoading,
                    recipes = recipes,
                    filteredRecipes = filteredRecipes,
                    searchQuery = searchQuery,
                    selectedRecipe = { selectedRecipe = it },
                    refreshState = refreshState
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                BookmarkHeaderSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onRefreshClicked = { refreshTrigger++ },
                    activeFilters = activeFilters,
                    onFilterClicked = { filter ->
                        activeFilters = if (filter in activeFilters)
                            activeFilters - filter
                        else
                            activeFilters + filter
                    },
                    modifier = Modifier
                        .background(Color(0xFF9dc484))
                        .fillMaxWidth()
                        .then(Modifier.widthIn(max = 400.dp))

                )

                BookmarkRecipeSection(
                    isLoading = isLoading,
                    recipes = recipes,
                    filteredRecipes = filteredRecipes,
                    searchQuery = searchQuery,
                    selectedRecipe = { selectedRecipe = it },
                    refreshState = refreshState
                )
            }
        }
    }
}



@Composable
fun BookmarkHeaderSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefreshClicked: () -> Unit,
    activeFilters: Set<RecipeFilter>,
    onFilterClicked: (RecipeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkGreen = Color(0xFF49891a)

    Column(
        modifier = modifier // Light green
            .padding(16.dp)
    ) {
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
                    if (newValue.length <= 45) {
                        onSearchQueryChange(newValue)
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
                    cursorColor = darkGreen
                ),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            Button(
                onClick = onRefreshClicked,
                modifier = Modifier.fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreen,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        // Filters
        if (isLandscape) {
            val chipRows = RecipeFilter.entries.chunked(2) // Break into rows with 2 items each

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                chipRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { filter ->
                            FilterChip(
                                selected = activeFilters.contains(filter),
                                onClick = { onFilterClicked(filter) },
                                label = { Text(filter.label) },
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(1f), // Distribute evenly within the row
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Black,
                                    selectedContainerColor = darkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Fill empty space if there's an odd number of filters
                        if (row.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(RecipeFilter.entries.toTypedArray()) { filter ->
                    FilterChip(
                        selected = activeFilters.contains(filter),
                        onClick = { onFilterClicked(filter) },
                        label = { Text(filter.label) },
                        modifier = Modifier.height(50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = Color.Black,
                            selectedContainerColor = darkGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarkRecipeSection(
    isLoading: Boolean,
    recipes: List<RecipePreview>,
    filteredRecipes: List<RecipePreview>,
    searchQuery: String,
    selectedRecipe: (RecipePreview) -> Unit,
    refreshState: PullRefreshState
) {
    val searchedRecipes = filteredRecipes.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(refreshState)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            recipes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
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
            searchedRecipes.isEmpty() && (searchQuery.isNotBlank() || filteredRecipes.isEmpty()) -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
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
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchedRecipes) { recipe ->
                        RecipePreviewCard(
                            recipe,
                            onViewClicked = { selectedRecipe(recipe) },
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