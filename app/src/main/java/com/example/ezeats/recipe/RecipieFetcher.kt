package com.example.ezeats.recipe

import android.text.Html
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.json.JSONException
import org.json.JSONTokener
import java.util.regex.Pattern
import org.jsoup.nodes.Document


// Function to fetch recipe previews from a list of URLs
suspend fun fetchRecipePreviews(urls: List<String>): List<RecipePreview> {
    // Use async to run the requests concurrently
    val deferredResults = urls.map { url ->
        GlobalScope.async(Dispatchers.IO) {
            fetchRecipePreview(url)
        }
    }

    // Wait for all the async tasks to complete and gather the results
    return deferredResults.awaitAll().filterNotNull() // Filter out null results
}



suspend fun fetchRecipePreview(url: String): RecipePreview? = withContext(Dispatchers.IO) {
    try {
        val doc = fetchRecipeFromUrl(url) ?: return@withContext null
        val recipeJson = extractRecipeJson(doc) ?: return@withContext null

        return@withContext parseRecipeJson(recipeJson, url)
    } catch (e: Exception) {
        println("Error loading URL: $url")
        e.printStackTrace()
        null
    }
}

// Helper function to fetch the document and extract the JSON-LD
fun fetchRecipeFromUrl(url: String): Document? {
    return try {
        Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(10000)
            .get()
    } catch (e: Exception) {
        println("Error fetching URL: $url")
        e.printStackTrace()
        null
    }
}

// Helper function to extract the first recipe JSON-LD from the document
fun extractRecipeJson(doc: Document): JSONObject? {
    val scriptTags = doc.select("script[type=application/ld+json]")
    if (scriptTags.isEmpty()) {
        println("No recipe JSON found")
        return null
    }

    for (tag in scriptTags) {
        try {
            val rawJson = tag.html()
            val parsed = JSONTokener(rawJson).nextValue()

            val candidates = mutableListOf<JSONObject>()
            when (parsed) {
                is JSONObject -> {
                    if (parsed.has("@graph")) {
                        val graph = parsed.getJSONArray("@graph")
                        for (i in 0 until graph.length()) {
                            val item = graph.optJSONObject(i)
                            if (item != null && isRecipeType(item)) candidates.add(item)
                        }
                    } else if (isRecipeType(parsed)) candidates.add(parsed)
                }
                is JSONArray -> {
                    for (i in 0 until parsed.length()) {
                        val item = parsed.optJSONObject(i)
                        if (item != null && isRecipeType(item)) candidates.add(item)
                    }
                }
            }

            return candidates.firstOrNull()
        } catch (inner: Exception) {
            continue
        }
    }
    return null
}

// Helper function to parse the recipe JSON data and return the RecipePreview
fun parseRecipeJson(recipeJson: JSONObject, url: String): RecipePreview {
    val rawTitle = recipeJson.optString("name", "Unknown Recipe")
    val title = Html.fromHtml(rawTitle, Html.FROM_HTML_MODE_LEGACY).toString()

    val image = recipeJson.opt("image")
    val imageUrl = when (image) {
        is JSONArray -> image.optString(0)
        is String -> image
        else -> ""
    }.ifBlank { "https://via.placeholder.com/300x200.png?text=No+Image" }

    val rawAuthor = recipeJson.optJSONObject("author")?.optString("name")
        ?: recipeJson.optJSONArray("author")?.optJSONObject(0)?.optString("name")
        ?: "Unknown"
    val author = Html.fromHtml(rawAuthor, Html.FROM_HTML_MODE_LEGACY).toString()

    val ratingObj = recipeJson.optJSONObject("aggregateRating")
    val rating = ratingObj?.optDouble("ratingValue") ?: 0.0
    val reviews = ratingObj?.optInt("reviewCount") ?: 0

    val rawTime = recipeJson.optString("totalTime", "")
    val time = if (rawTime.startsWith("PT")) {
        parseDuration(rawTime)
    } else {
        rawTime.takeIf { it.isNotEmpty() && it[0].isDigit() } ?: "N/A"
    }

    val ingredientsArray = recipeJson.optJSONArray("recipeIngredient")
    val ingredients = mutableListOf<String>()
    for (i in 0 until (ingredientsArray?.length() ?: 0)) {
        ingredients.add(ingredientsArray!!.getString(i))
    }

    return RecipePreview(
        title = title,
        imageUrl = imageUrl,
        author = author,
        rating = rating,
        reviews = reviews,
        time = time,
        ingredients = ingredients,
        url = url
    )
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
            .takeIf { it.isNotEmpty() } ?: "N/A"
    } else {
        "N/A"
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

