package com.example.ezeats.recipe

//Data class for the Recipe preview
data class RecipePreview(
    val title: String,
    val imageUrl: String,
    val author: String,
    val rating: Double?,
    val reviews: Int?,
    val time: String,
    val ingredients: List<String>,
    val url: String
)