package com.example.ezeats.recipe

import android.text.Html
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.json.JSONTokener
import java.util.regex.Pattern
import org.jsoup.nodes.Document


object UserAgentPool {
    val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:115.0) Gecko/20100101 Firefox/115.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.4896.127 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64; rv:110.0) Gecko/20100101 Firefox/110.0",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (iPad; CPU OS 16_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 10; SAMSUNG SM-G970U) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/13.2 Chrome/83.0.4103.106 Mobile Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.4896.127 Safari/537.36 OPR/95.0.4635.90",
        "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36 Brave/1.63.165",
        "Mozilla/5.0 (X11; CrOS x86_64 14526.83.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.224 Safari/537.36",
        "Mozilla/5.0 (Linux; U; Android 10; en-US; SM-A107F) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 UCBrowser/13.4.0.1306 Mobile Safari/537.36"
    )
}

// Function to fetch recipe previews from a list of URLs
suspend fun fetchRecipePreviews(urls: List<String>): List<RecipePreview> {
    // Use async to run the requests concurrently
    return coroutineScope {
        urls.map { url ->
            async(Dispatchers.IO) { fetchRecipePreview(url) }
        }.awaitAll().filterNotNull()
    }

    // Wait for all the async tasks to complete and gather the results
    //return deferredResults.awaitAll().filterNotNull() // Filter out null results
}



suspend fun fetchRecipePreview(url: String): RecipePreview? {
    val doc = fetchRecipeFromUrl(url) ?: return null

    // Attempt to extract structured data (JSON-LD)
    val json = extractRecipeJson(doc)

    return json?.let { parseJsonToRecipePreview(it, url) }
        ?: fallbackScrapeRecipe(doc, url)
}

// Helper function to fetch the document and extract the JSON-LD
suspend fun fetchRecipeFromUrl(url: String): Document? = withContext(Dispatchers.IO) {
    // Introduce a random delay (between 800ms and 1500ms)
    delay((800..1500).random().toLong())

    val randomUserAgent = UserAgentPool.userAgents.random()

    return@withContext try {
        withTimeout(15_000) {
            Jsoup.connect(url)
                .userAgent(randomUserAgent)
                .timeout(15_000)
                .get()
        }
    } catch (e: Exception) {
        println("Error fetching URL: $url")
        e.printStackTrace()
        null
    }
}

// Helper function to extract the first recipe JSON-LD from the document
fun extractRecipeJson(doc: Document): JSONObject? {
    val scriptTags = doc.select("script[type=application/ld+json]")
    for (tag in scriptTags) {
        val rawJson = tag.html().trim()
        try {
            val parsedJson = JSONTokener(rawJson).nextValue()

            // Handle case when there's a single Recipe object
            if (parsedJson is JSONObject && parsedJson.optString("@type").contains("Recipe", ignoreCase = true)) {
                return parsedJson
            }

            // Handle case for @graph array containing multiple objects
            if (parsedJson is JSONObject && parsedJson.has("@graph")) {
                val graph = parsedJson.getJSONArray("@graph")
                for (i in 0 until graph.length()) {
                    val entry = graph.getJSONObject(i)
                    if (entry.optString("@type").contains("Recipe", ignoreCase = true)) {
                        return entry
                    }
                }
            }

            // Handle case for array of Recipe objects
            if (parsedJson is JSONArray) {
                for (i in 0 until parsedJson.length()) {
                    val obj = parsedJson.getJSONObject(i)
                    if (obj.optString("@type").contains("Recipe", ignoreCase = true)) {
                        return obj
                    }
                }
            }

        } catch (e: Exception) {
            println("Failed to parse JSON-LD from: $rawJson")
        }
    }

    println("No recipe JSON found in structured data")
    return null
}

// Parse the structured JSON to RecipePreview
fun parseJsonToRecipePreview(json: JSONObject, url: String): RecipePreview {
    val title = json.optString("name")
    val imageUrl = json.optString("image").takeIf { it.isNotEmpty() } ?: "your_default_image_url" // Default image URL if empty
    val author = json.optString("author")

    //val rating = json.optJSONObject("aggregateRating")?.optDouble("ratingValue")
    val rawRating = json.optJSONObject("aggregateRating")?.optDouble("ratingValue")
    val rating = rawRating?.takeIf { !it.isNaN() }

    val reviews = json.optJSONObject("aggregateRating")?.optInt("reviewCount")
    val time = json.optString("cookTime")
    val parsedTime = parseDuration(time)
    //val ingredients = json.optJSONArray("recipeIngredient")?.join(", ") ?: "N/A"
    val ingredients = json.optJSONArray("recipeIngredient")?.let {
        // Convert the JSONArray to a List of Strings
        List(it.length()) { index -> it.getString(index) }
    } ?: listOf("Ingredients Unavailable")

    return RecipePreview(title, imageUrl, author, rating, reviews, parsedTime, ingredients, url)
}

// Fallback method to scrape if structured data is not found
fun fallbackScrapeRecipe(doc: Document, url: String): RecipePreview {
    val title = doc.select("meta[property=og:title]").attr("content").ifBlank { doc.title() }
    val image = doc.select("meta[property=og:image]").attr("content").takeIf { it.isNotEmpty() } ?: "your_default_image_url" // Default image URL if empty
    val author = doc.select("meta[name=author]").attr("content").ifBlank { "Unknown" }

    //val rating = doc.select("span[itemprop=ratingValue]").text().toDoubleOrNull()
    val rawRating = doc.select("span[itemprop=ratingValue]").text().toDoubleOrNull()
    val rating = rawRating?.takeIf { !it.isNaN() }

    val reviews = doc.select("span[itemprop=reviewCount]").text().toIntOrNull()
    val time = doc.select("meta[itemprop=cookTime]").attr("content").ifBlank { "Unknown" }
    val parsedTime = parseDuration(time)
    //val ingredients = doc.select("ul.recipe-ingredients li").joinToString(", ") { it.text() }
    val ingredients = doc.select("ul.recipe-ingredients li")
        .map { it.text() }
        .takeIf { it.isNotEmpty() } ?: listOf("Ingredients Unavailable")

    return RecipePreview(title, image, author, rating, reviews, parsedTime, ingredients, url)
}

fun parseDuration(duration: String): String {
    val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
    val matcher = pattern.matcher(duration)

    return if (matcher.matches()) {
        val hours = matcher.group(1)?.toIntOrNull() ?: 0
        val minutes = matcher.group(2)?.toIntOrNull() ?: 0
        val seconds = matcher.group(3)?.toIntOrNull() ?: 0

        // If the time is 0, set it to 30 minutes
        val finalHours = if (hours == 0 && minutes == 0 && seconds == 0) 0 else hours
        val finalMinutes = if (hours == 0 && minutes == 0 && seconds == 0) 30 else minutes
        val finalSeconds = if (hours == 0 && minutes == 0 && seconds == 0) 0 else seconds

        // Construct a human-readable time string
        val timeParts = mutableListOf<String>()
        if (finalHours > 0) timeParts.add("${finalHours}h")
        if (finalMinutes > 0) timeParts.add("${finalMinutes}m")
        if (finalSeconds > 0) timeParts.add("${finalSeconds}s")

        // Return the formatted time, or "N/A" if nothing was matched
        timeParts.joinToString(" ") { it }
            .takeIf { it.isNotEmpty() } ?: "30m+"
    } else {
        "30m+"
    }
}




private fun isRecipeType(json: JSONObject): Boolean {
    val type = json.opt("@type")
    return when (type) {
        is String -> type.equals("Recipe", ignoreCase = true)
        is JSONArray -> (0 until type.length()).any { i ->
            type.optString(i).equals("Recipe", ignoreCase = true)
        }
        else -> false
    }
}


private fun JSONArray.findRecipeJson(): JSONObject? {
    for (i in 0 until this.length()) {
        val obj = this.getJSONObject(i)
        if (obj.optString("@type") == "Recipe") return obj
    }
    return null
}

