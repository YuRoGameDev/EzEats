package com.example.ezeats.Screens
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ezeats.DatabaseProvider
import com.example.ezeats.recipe.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

// Function to search for recipes and fetch the top 10 URLs
suspend fun searchRecipes(query: String, querySize: Int, timeLimitMs: Long): List<String> = withContext(Dispatchers.IO) {
    val apiKey = "AIzaSyBIY4Za1x9wIaWCoYhh0-x64TFAW-hOgCg"
    val cseId = "13f9e92f2347c46c6"
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val urls = mutableListOf<String>()

    try {
        var startIndex = 1
        val startTime = System.currentTimeMillis()

        while (urls.size < querySize) {
            // Check if time limit has been exceeded
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
            if (items == null || items.length() == 0) break  // No more results

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val link = item.optString("link")
                if (link.startsWith("http")) {
                    urls.add(link)
                    println(link)
                    if (urls.size >= querySize) break
                }
            }

            // Increment by 10 for the next page
            startIndex += 10
            delay(1000) // Be polite and avoid rate limiting
        }
    } catch (e: TimeoutCancellationException) {
        println("Search operation timed out.")
    } catch (e: Exception) {
        e.printStackTrace()
    }

    urls.take(querySize)
}

suspend fun searchDuckRecipes(query: String, querySize: Int, timeLimitMs: Long): List<String> = withContext(Dispatchers.IO) {
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
            // Check if time limit has been exceeded
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
                    // Avoid adding duplicate URLs
                    if (!urls.contains(href)) {
                        urls.add(href)
                        println(href)
                    }
                    if (urls.size >= querySize) break
                }
            }

            start += 50 // Move to the next page (increment the start by 50)
            delay(1000) // Be polite and avoid rate limiting
        }
    } catch (e: TimeoutCancellationException) {
        println("Search operation timed out.")
    } catch (e: Exception) {
        e.printStackTrace()
    }

    urls.take(querySize).toList()
}


// Composable function to display the search screen
@Composable
fun SearchScreen() {
    BackHandler(enabled = true) {}

    val coroutineScope = rememberCoroutineScope()
    var searchTerm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<RecipePreview>>(emptyList()) }
    var selectedRecipe by remember { mutableStateOf<RecipePreview?>(null) }
    var activeFilters by remember { mutableStateOf(setOf<RecipeFilter>()) }

    val filteredRecipes = filterAndSortRecipes(recipes, activeFilters)

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
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                label = { Text("Search Recipes (e.g. Cookies)") },
                modifier = Modifier.fillMaxWidth()
            )

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
                        label = { Text(filter.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        recipes = emptyList()

                        // Fetch recipe URLs from the search query
                        val urls = searchRecipes(searchTerm, 25, 10000)
                        //val urls = searchDuckRecipes(searchTerm,25, 10000)

                        // Fetch the previews for each recipe URL
                        recipes = fetchRecipePreviews(urls)
                        isLoading = false
                    }
                },
                enabled = searchTerm.isNotBlank()
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> CircularProgressIndicator()
                filteredRecipes.isNotEmpty() -> {
                    LazyColumn {
                        items(filteredRecipes) { recipe ->
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


