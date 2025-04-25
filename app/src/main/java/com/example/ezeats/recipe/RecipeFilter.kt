package com.example.ezeats.recipe

enum class RecipeFilter(val label: String) {
    GLUTEN_FREE("Gluten-Free"),
    DAIRY_FREE("Dairy-Free"),
    NUT_FREE("No Nuts"),
    HIGH_RATING("Top Rated"),
    QUICK("Under 30 Min")
}

fun filterAndSortRecipes(
    recipes: List<RecipePreview>,
    activeFilters: Set<RecipeFilter>
): List<RecipePreview> {
    return recipes.filter { recipe ->
        var match = true

        val titleLower = recipe.title.lowercase()

        if (RecipeFilter.GLUTEN_FREE in activeFilters) {
            val containsGluten = recipe.ingredients.any { it.contains("gluten", ignoreCase = true) } ||
                    titleLower.contains("gluten")
            match = match && !containsGluten
        }

        if (RecipeFilter.DAIRY_FREE in activeFilters) {
            val dairyKeywords = listOf("milk", "cheese", "butter", "cream", "yogurt")
            val containsDairy = recipe.ingredients.any { ingredient ->
                dairyKeywords.any { dairy -> ingredient.contains(dairy, ignoreCase = true) }
            } || dairyKeywords.any { titleLower.contains(it) }
            match = match && !containsDairy
        }

        if (RecipeFilter.NUT_FREE in activeFilters) {
            val nutKeywords = listOf("almond", "peanut", "cashew", "walnut", "nut")
            val containsNuts = recipe.ingredients.any { ingredient ->
                nutKeywords.any { nut -> ingredient.contains(nut, ignoreCase = true) }
            } || nutKeywords.any { titleLower.contains(it) }
            match = match && !containsNuts
        }

        if (RecipeFilter.QUICK in activeFilters) {
            val timeRegex = Regex("""\d+""")
            val minutes = timeRegex.find(recipe.time)?.value?.toIntOrNull() ?: Int.MAX_VALUE
            match = match && minutes <= 30
        }

        match
    }.let { filtered ->
        if (RecipeFilter.HIGH_RATING in activeFilters) {
            filtered.sortedByDescending { it.rating ?: 0.0 }
        } else {
            filtered
        }
    }
}