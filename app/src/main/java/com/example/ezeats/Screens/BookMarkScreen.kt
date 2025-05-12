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

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.recipe.*

import kotlinx.coroutines.launch
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

import com.example.ezeats.ui.theme.darkGreen

//Handles the visuals for the book mark screen.
//This is the same as the search screen and Im too lazy to put comments in each section
//But only difference is instead of searching for the recipe, it automatically gets and populates
//with the urls from the local room storage.
//Also there is an additional search field to filter bookmarked recipes
//Users can also refresh the list by swiping up
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookMarkScreen() {
    BackHandler(enabled = true) {}
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<RecipePreview>>(emptyList()) }
    var selectedRecipe by remember { mutableStateOf<RecipePreview?>(null) }
    var bookmarkedUrls by remember { mutableStateOf(DatabaseProvider.getBookmarkedUrls()) }
    var refreshTrigger by remember { mutableStateOf(0) }
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

            recipes = fetchRecipePreviews(bookmarkedUrls)
            isLoading = false
            refreshing.value = false
        }
    }

    if (selectedRecipe != null) {
        RecipeWebView(
            url = selectedRecipe!!.url,
            onBack = { selectedRecipe = null }
        )
    } else {
        val refreshState = rememberPullRefreshState(
            refreshing = refreshing.value,
            onRefresh = {
                refreshing.value = true
                refreshTrigger++
            },
            refreshThreshold = 100.dp
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
    Column(
        modifier = modifier
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
                singleLine = true,
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

        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) {
            val chipRows = RecipeFilter.entries.chunked(2)

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
                                    .weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Black,
                                    selectedContainerColor = darkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

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