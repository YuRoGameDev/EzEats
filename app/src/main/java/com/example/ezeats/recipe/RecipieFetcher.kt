package com.example.ezeats.recipe

import android.text.Html
import kotlinx.coroutines.*
import org.json.*
import org.jsoup.Jsoup
import java.util.regex.Pattern
import org.jsoup.nodes.Document

//As to avoid getting blocked from parsing the website, a lot of agents have to be made
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

//Goes through all the recipes. Its all being done on a different thread
suspend fun fetchRecipePreviews(urls: List<String>): List<RecipePreview> {
    return coroutineScope {
        urls.map { url ->
            async(Dispatchers.IO) { fetchRecipePreview(url) }
        }.awaitAll().filterNotNull()
    }

}

//Gets the individual data for a recipe and parses it
suspend fun fetchRecipePreview(url: String): RecipePreview? {
    val doc = fetchRecipeFromUrl(url) ?: return null

    val json = extractRecipeJson(doc)

    return json?.let { parseJsonToRecipePreview(it, url) }
        ?: fallbackScrapeRecipe(doc, url)
}

//Ths gets the jsoin html file using Jsoup
suspend fun fetchRecipeFromUrl(url: String): Document? = withContext(Dispatchers.IO) {
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

//The html file from Jsoup is then extracted and parsed
//This is roughly 75% accurate with the 20 supported recipe websites
//Due to website html being structered differently, some data may be missing
fun extractRecipeJson(doc: Document): JSONObject? {
    val scriptTags = doc.select("script[type=application/ld+json]")
    for (tag in scriptTags) {
        val rawJson = tag.html().trim()
        try {
            val parsedJson = JSONTokener(rawJson).nextValue()

            if (parsedJson is JSONObject && parsedJson.optString("@type")
                    .contains("Recipe", ignoreCase = true)
            ) {
                return parsedJson
            }

            if (parsedJson is JSONObject && parsedJson.has("@graph")) {
                val graph = parsedJson.getJSONArray("@graph")
                for (i in 0 until graph.length()) {
                    val entry = graph.getJSONObject(i)
                    if (entry.optString("@type").contains("Recipe", ignoreCase = true)) {
                        return entry
                    }
                }
            }

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

//This is the general Json parser, getting all the data and converting it to a RecipePreview
fun parseJsonToRecipePreview(json: JSONObject, url: String): RecipePreview {
    val title = decodeHtml(json.optString("name")).replaceFirstChar { it.uppercase() }
    val imageUrl = json.optString("image").takeIf { it.isNotEmpty() } ?: "your_default_image_url"
    val author = json.optString("author")

    val rawRating = json.optJSONObject("aggregateRating")?.optDouble("ratingValue")
    val rating = rawRating?.takeIf { !it.isNaN() }

    val reviews = json.optJSONObject("aggregateRating")?.optInt("reviewCount")
    val time = json.optString("cookTime")
    val parsedTime = parseDuration(time)

    val ingredients = json.optJSONArray("recipeIngredient")?.let {
        List(it.length()) { index -> it.getString(index) }
    } ?: listOf("Ingredients Unavailable")

    return RecipePreview(title, imageUrl, author, rating, reviews, parsedTime, ingredients, url)
}

// Fallback method to scrape if structured data is not found
fun fallbackScrapeRecipe(doc: Document, url: String): RecipePreview {
    val title =
        decodeHtml(doc.select("meta[property=og:title]").attr("content").ifBlank { doc.title() })
            .replaceFirstChar { it.uppercase() }
    val image = doc.select("meta[property=og:image]").attr("content").takeIf { it.isNotEmpty() }
        ?: "your_default_image_url"
    val author = doc.select("meta[name=author]").attr("content").ifBlank { "Unknown" }

    val rawRating = doc.select("span[itemprop=ratingValue]").text().toDoubleOrNull()
    val rating = rawRating?.takeIf { !it.isNaN() }

    val reviews = doc.select("span[itemprop=reviewCount]").text().toIntOrNull()
    val time = doc.select("meta[itemprop=cookTime]").attr("content").ifBlank { "Unknown" }
    val parsedTime = parseDuration(time)

    val ingredients = doc.select("ul.recipe-ingredients li")
        .map { it.text() }
        .takeIf { it.isNotEmpty() } ?: listOf("Ingredients Unavailable")

    return RecipePreview(title, image, author, rating, reviews, parsedTime, ingredients, url)
}

//This converts the time to minutes be readable
fun parseDuration(duration: String): String {
    val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
    val matcher = pattern.matcher(duration)

    return if (matcher.matches()) {
        val hours = matcher.group(1)?.toIntOrNull() ?: 0
        val minutes = matcher.group(2)?.toIntOrNull() ?: 0
        val seconds = matcher.group(3)?.toIntOrNull() ?: 0

        val finalHours = if (hours == 0 && minutes == 0 && seconds == 0) 0 else hours
        val finalMinutes = if (hours == 0 && minutes == 0 && seconds == 0) 30 else minutes
        val finalSeconds = if (hours == 0 && minutes == 0 && seconds == 0) 0 else seconds

        val timeParts = mutableListOf<String>()
        if (finalHours > 0) timeParts.add("${finalHours}h")
        if (finalMinutes > 0) timeParts.add("${finalMinutes}m")
        if (finalSeconds > 0) timeParts.add("${finalSeconds}s")

        timeParts.joinToString(" ") { it }
            .takeIf { it.isNotEmpty() } ?: "30m+"
    } else {
        "30m+"
    }
}

//Fix for the ' character since that comes out as &39; in html
fun decodeHtml(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
}



