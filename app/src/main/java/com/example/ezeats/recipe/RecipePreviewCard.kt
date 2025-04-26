package com.example.ezeats.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.R

@Composable
fun RecipePreviewCard(recipe: RecipePreview, onViewClicked: (RecipePreview) -> Unit, onBookmarkClicked: (String) -> Unit) {
    var isBookmarked by remember { mutableStateOf(DatabaseProvider.isBookmarked(recipe.url)) }

    // Default image URL to be used if the recipe image is missing
    val defaultImageUrl = "https://github.com/YuRoGameDev/EzEats/blob/main/icons/home.png"

    val title = recipe.title.takeIf { it.isNotEmpty() } ?: "Title Unavailable"
    val imageUrl = recipe.imageUrl.takeIf { it.isNotEmpty() } ?: defaultImageUrl
    val darkGreen = Color(0xFF49891a)
    val lightGreen = Color(0xFF9dc484)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Let height be defined by content
        shape = RoundedCornerShape(16.dp),
        colors = CardColors(
            containerColor = lightGreen,
            contentColor = Color.White,
            disabledContainerColor = lightGreen,
            disabledContentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val imagePainter = rememberAsyncImagePainter(
                model = imageUrl,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder),)
            val imageState = imagePainter.state

            Box(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight() // Fixed size for the image container
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Show loading bar if the image is loading
                if (imageState is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(30.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Scale the image based on the container size
                Image(
                    painter = imagePainter,
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .fillMaxSize() // This makes the image scale to the container's size
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop // This ensures the aspect ratio is maintained
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)

                // Use "N/A" if rating is null
                val displayRating = recipe.rating?.let { "⭐ $it" } ?: "⭐ N/A"
                Text("Time: ${recipe.time} - $displayRating", style = MaterialTheme.typography.bodySmall)

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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onViewClicked(recipe) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu, // or any icon you prefer
                            contentDescription = "View",
                            modifier = Modifier.size(52.dp)

                        )
                    }

                    Button(
                        onClick = {
                            onBookmarkClicked(recipe.url)
                            isBookmarked = !isBookmarked
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Image(
                            painter = painterResource(id = if (isBookmarked) R.drawable.bookmarked else R.drawable.bookmark_none),
                            contentDescription = "Bookmark",
                            modifier = Modifier.size(52.dp), // Adjust size as needed
                        )
                    }
                }
            }
        }
    }
}



