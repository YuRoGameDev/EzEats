package com.example.ezeats.Screens

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.recipe.*

import kotlinx.coroutines.*

import org.json.JSONObject
import org.jsoup.Jsoup

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp

import com.example.ezeats.Credentials
import com.example.ezeats.ui.theme.*
import kotlin.collections.plus

//This handles the visuals for the search screen.
//I am too lazy to comment this all in detail, but generally
//The user searches for recipes, it connects to google api (or duckduckGo incase quota is met)
//Then the screen is populated with recipes the user can book mark. When clicking on one a web view is opened
//Same like the bookmark screen it is designed customally for landscaped mode
//There was some other stuff added but Im too tired to explain it, it just works and looks cool
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun SearchScreen() {
    BackHandler(enabled = true) {}

    val coroutineScope = rememberCoroutineScope()
    var searchTerm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<RecipePreview>>(emptyList()) }
    var selectedRecipe by remember { mutableStateOf<RecipePreview?>(null) }
    var activeFilters by remember { mutableStateOf(setOf<RecipeFilter>()) }

    val filteredRecipes = filterAndSortRecipes(recipes, activeFilters)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (selectedRecipe != null) {
        RecipeWebView(
            url = selectedRecipe!!.url,
            onBack = { selectedRecipe = null }
        )
    } else {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                HeaderSection(
                    searchTerm = searchTerm,
                    onSearchTermChange = { searchTerm = it },
                    onSearchClicked = {
                        coroutineScope.launch {
                            isLoading = true
                            recipes = emptyList()

                            val urls = searchRecipesWithFallback(searchTerm, 25, 10000)
                            recipes = fetchRecipePreviews(urls)
                            isLoading = false
                        }
                    },
                    activeFilters = activeFilters,
                    onFilterClicked = {
                        activeFilters = if (it in activeFilters)
                            activeFilters - it
                        else
                            activeFilters + it
                    },
                    modifier = Modifier
                        .background(Color(0xFF7a9c65))
                        .fillMaxHeight()
                        .width(350.dp)
                )

                RecipeSection(
                    isLoading = isLoading,
                    recipes = recipes,
                    filteredRecipes = filteredRecipes,
                    searchTerm = searchTerm,
                    searched = searched,
                    onRecipeClicked = { selectedRecipe = it }
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderSection(
                    searchTerm = searchTerm,
                    onSearchTermChange = { searchTerm = it },
                    onSearchClicked = {
                        coroutineScope.launch {
                            isLoading = true
                            recipes = emptyList()

                            val urls = searchRecipesWithFallback(searchTerm, 25, 10000)
                            recipes = fetchRecipePreviews(urls)
                            isLoading = false
                        }
                    },
                    activeFilters = activeFilters,
                    onFilterClicked = {
                        activeFilters = if (it in activeFilters)
                            activeFilters - it
                        else
                            activeFilters + it
                    },
                    modifier = Modifier
                        .background(Color(0xFF9dc484))
                        .fillMaxWidth()
                        .then(Modifier.widthIn(max = 400.dp))
                )

                RecipeSection(
                    isLoading = isLoading,
                    recipes = recipes,
                    filteredRecipes = filteredRecipes,
                    searchTerm = searchTerm,
                    searched = searched,
                    onRecipeClicked = { selectedRecipe = it }
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
    searchTerm: String,
    onSearchTermChange: (String) -> Unit,
    onSearchClicked: () -> Unit,
    activeFilters: Set<RecipeFilter>,
    onFilterClicked: (RecipeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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
            val keyboardController = LocalSoftwareKeyboardController.current

            OutlinedTextField(
                value = searchTerm,
                onValueChange = { newValue ->
                    if (newValue.length <= 45) {
                        onSearchTermChange(newValue)
                    }
                },
                label = { Text("Search for some Recipes!") },
                modifier = Modifier.weight(1f),
                singleLine = true, // Read the line
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClicked()
                        keyboardController?.hide()
                    }
                ),
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
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

            )

            Button(
                onClick = { onSearchClicked() },
                enabled = searchTerm.isNotBlank(),
                modifier = Modifier.fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreen

,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
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
                                    selectedContainerColor = darkGreen

,
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
                            selectedContainerColor = darkGreen

,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeSection(
    isLoading: Boolean,
    recipes: List<RecipePreview>,
    filteredRecipes: List<RecipePreview>,
    searchTerm: String,
    searched: Boolean,
    onRecipeClicked: (RecipePreview) -> Unit,
) {
    Spacer(modifier = Modifier.height(16.dp))

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        recipes.isEmpty() && !searched -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Search to find some recipes!", color = Color.Black, fontSize = 30.sp)
            }
        }

        recipes.isEmpty() && searchTerm.isNotBlank() && searched -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No results found", color = Color.Black, fontSize = 30.sp)
            }
        }

        filteredRecipes.isNotEmpty() -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecipes) { recipe ->
                    RecipePreviewCard(
                        recipe,
                        onViewClicked = { onRecipeClicked(recipe) },
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

//This is to search for recipes. If Google Apis quota is met go to duckduckGo
suspend fun searchRecipesWithFallback(
    searchTerm: String,
    numResults: Int,
    maxResults: Long
): List<String> {
    return try {
        searchRecipes(searchTerm, numResults, maxResults)
    } catch (e: Exception) {
        println("Google API rate limit reached or error occurred: ${e.message}")

        searchDuckRecipes(searchTerm, numResults, maxResults)
    }
}

//The main google api search. Works well, pretty fast, and gets the full list of recipes
//because google paginates. However Im only using free tier and I aint wasting my money so
//the quota can get filled up pretty quickly if more than 5+ people are searching at once every second
suspend fun searchRecipes(query: String, querySize: Int, timeLimitMs: Long): List<String> =
    withContext(Dispatchers.IO) {
        val apiKey = Credentials.GOOGLE_SEARCH_API_KEY
        val cseId = Credentials.GOOGLE_CSE_ID
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urls = mutableListOf<String>()

        try {
            var startIndex = 1
            val startTime = System.currentTimeMillis()

            while (urls.size < querySize) {
                if (System.currentTimeMillis() - startTime > timeLimitMs) {
                    println("Time limit exceeded.")
                    break
                }

                val searchUrl =
                    "https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$cseId&q=$encodedQuery&start=$startIndex"
                val url = URL(searchUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)

                val items = json.optJSONArray("items")
                if (items == null || items.length() == 0) break

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val link = item.optString("link")
                    if (link.startsWith("http")) {
                        urls.add(link)
                        println(link)
                        if (urls.size >= querySize) break
                    }
                }

                startIndex += 10
                delay(1000)
            }
        } catch (e: TimeoutCancellationException) {
            println("Search operation timed out.")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        urls.take(querySize)
    }

//The slower shittier version of the google search. The engine doesnt have pagination or domain
//filtering so I gotta do it customally. its very slow and its timedout to 10 seconds. At most you
//can maybe get like 5, maybe 10 recipes if lucky
suspend fun searchDuckRecipes(query: String, querySize: Int, timeLimitMs: Long): List<String> =
    withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode("$query recipe", "UTF-8")
        val urls = mutableSetOf<String>()

        val allowedDomains = listOf(
            "allrecipes.com", "seriouseats.com", "epicurious.com", "foodnetwork.com",
            "bbcgoodfood.com", "tasty.co", "cooking.nytimes.com", "bonappetit.com",
            "thekitchn.com", "smittenkitchen.com", "minimalistbaker.com", "halfbakedharvest.com",
            "simplyrecipes.com", "loveandlemons.com", "cookieandkate.com", "damndelicious.net",
            "gimmesomeoven.com", "budgetbytes.com", "chowhound.com", "eatingwell.com"
        )

        try {
            var start = 0
            val startTime = System.currentTimeMillis()

            while (urls.size < querySize) {
                if (System.currentTimeMillis() - startTime > timeLimitMs) {
                    println("Time limit exceeded.")
                    break
                }

                val duckUrl = "https://html.duckduckgo.com/html/?q=$encodedQuery&s=$start"
                val doc = Jsoup.connect(duckUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get()

                val results = doc.select("a.result__a")
                if (results.isEmpty()) break

                for (element in results) {
                    val href = element.absUrl("href")
                    if (href.startsWith("http") && allowedDomains.any { href.contains(it) }) {
                        if (!urls.contains(href)) {
                            urls.add(href)
                            println(href)
                        }
                        if (urls.size >= querySize) break
                    }
                }

                start += 50
                delay(1000)
            }
        } catch (e: TimeoutCancellationException) {
            println("Search operation timed out.")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        urls.take(querySize).toList()
    }
