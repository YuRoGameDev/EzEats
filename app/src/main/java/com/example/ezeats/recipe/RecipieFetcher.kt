package com.example.ezeats.recipe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

suspend fun fetchRecipePreview(url: String): RecipePreview? = withContext(Dispatchers.IO) {
    try {
        val doc = Jsoup.connect(url).get()
        val scriptTags = doc.select("script[type=application/ld+json]")

        for (tag in scriptTags) {
            val json = tag.html()
            val jsonObject = JSONObject(json)

            val recipeJson = if (jsonObject.has("@graph")) {
                val graph = jsonObject.getJSONArray("@graph")
                graph.findRecipeJson()
            } else if (jsonObject.optString("@type") == "Recipe") {
                jsonObject
            } else {
                null
            }

            recipeJson?.let {
                val title = it.optString("name")
                val image = it.opt("image")
                val imageUrl = when (image) {
                    is JSONArray -> image.getString(0)
                    is String -> image
                    else -> ""
                }

                val author = it.optJSONObject("author")?.optString("name") ?: "Unknown"
                val rating = it.optJSONObject("aggregateRating")?.optDouble("ratingValue") ?: 0.0
                val reviews = it.optJSONObject("aggregateRating")?.optInt("reviewCount") ?: 0
                val time = it.optString("totalTime").replace("PT", "").lowercase()

                val ingredientsArray = it.optJSONArray("recipeIngredient")
                val ingredients = mutableListOf<String>()
                for (i in 0 until (ingredientsArray?.length() ?: 0)) {
                    ingredients.add(ingredientsArray!!.getString(i))
                }

                return@withContext RecipePreview(
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
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun JSONArray.findRecipeJson(): JSONObject? {
    for (i in 0 until this.length()) {
        val obj = this.getJSONObject(i)
        if (obj.optString("@type") == "Recipe") return obj
    }
    return null
}