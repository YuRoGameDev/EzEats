package com.example.ezeats.recipe

enum class RecipeFilter(val label: String) {
    HIGH_RATING("Top Rated"),
    QUICK("Under 30 Min"),
    NUT_FREE("No Nuts"),
    DAIRY_FREE("Dairy-Free"),
    GLUTEN_FREE("Gluten-Free"),
}

//Filters the list of recipe Previews
fun filterAndSortRecipes(
    recipes: List<RecipePreview>,
    activeFilters: Set<RecipeFilter>
): List<RecipePreview> {
    return recipes.filter { recipe ->
        var match = true

        val titleLower = recipe.title.lowercase()

        if (RecipeFilter.GLUTEN_FREE in activeFilters) {
            val containsGluten =
                recipe.ingredients.any { it.contains("gluten", ignoreCase = true) } ||
                        titleLower.contains("gluten")
            match = match && !containsGluten
            println("After Gluten-Free filter: ${if (containsGluten) "Excluded" else "Included"} recipe: ${recipe.title}")
        }

        if (RecipeFilter.DAIRY_FREE in activeFilters) {
            val dairyKeywords = listOf("milk", "cheese", "butter", "cream", "yogurt")
            val containsDairy = recipe.ingredients.any { ingredient ->
                dairyKeywords.any { dairy -> ingredient.contains(dairy, ignoreCase = true) }
            } || dairyKeywords.any { titleLower.contains(it) }
            match = match && !containsDairy
            println("After Dairy-Free filter: ${if (containsDairy) "Excluded" else "Included"} recipe: ${recipe.title}")
        }

        if (RecipeFilter.NUT_FREE in activeFilters) {
            val nutKeywords = listOf("almond", "peanut", "cashew", "walnut", "nut")
            val containsNuts = recipe.ingredients.any { ingredient ->
                nutKeywords.any { nut -> ingredient.contains(nut, ignoreCase = true) }
            } || nutKeywords.any { titleLower.contains(it) }
            match = match && !containsNuts
            println("After Nut-Free filter: ${if (containsNuts) "Excluded" else "Included"} recipe: ${recipe.title}")
        }

        if (RecipeFilter.QUICK in activeFilters) {
            val time = recipe.time
            val number = time.replace(Regex("[^0-9]"), "").toInt()

            match = match && number < 30
            println("After Quick filter (<= 30 mins): ${if (number <= 30) "Included" else "Excluded"} recipe: ${recipe.title}")
        }

        match
    }.let { filtered -> //This sorts based off Rating rather than excluding recipes
        println("Recipes after filtering: ${filtered.size} out of ${recipes.size} total")

        if (RecipeFilter.HIGH_RATING in activeFilters) {
            val sorted = filtered.sortedByDescending { it.rating ?: 0.0 }
            println("After High Rating filter: ${sorted.size} recipes with high ratings")
            sorted
        } else {
            filtered
        }
    }
}