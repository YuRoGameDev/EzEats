package com.example.ezeats.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun RecipePreviewCard(recipe: RecipePreview) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(recipe.imageUrl),
                contentDescription = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(recipe.title, style = MaterialTheme.typography.titleLarge)
            Text("By ${recipe.author}", style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.height(4.dp))

            Text("⭐ ${recipe.rating}  (${recipe.reviews} reviews)", style = MaterialTheme.typography.bodySmall)
            Text("Time: ${recipe.time}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Ingredients:", style = MaterialTheme.typography.labelLarge)
            recipe.ingredients.take(5).forEach {
                Text("• $it", style = MaterialTheme.typography.bodySmall)
            }

            if (recipe.ingredients.size > 5) {
                Text("...and more", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}