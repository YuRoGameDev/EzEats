package com.example.ezeats.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun RecipePreviewCard(recipe: RecipePreview, onViewClicked: (RecipePreview) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Let height be defined by content
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(recipe.imageUrl),
                contentDescription = recipe.title,
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(recipe.title, style = MaterialTheme.typography.titleMedium)

                Text("Time: ${recipe.time} - ⭐ ${recipe.rating}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    Text("Ingredients:", style = MaterialTheme.typography.labelLarge)
                    recipe.ingredients.take(3).forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall)
                    }

                    if (recipe.ingredients.size > 3) {
                        Text("...and more", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onViewClicked(recipe) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text("View Full Recipe")
                }
            }
        }
    }
}
