package com.example.ezeats.recipe

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*

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
import com.example.ezeats.ui.theme.*

//This is a card handling the visuals for the individual recipes found
@Composable
fun RecipePreviewCard(
    recipe: RecipePreview,
    onViewClicked: (RecipePreview) -> Unit,
    onBookmarkClicked: (String) -> Unit
) {
    var isBookmarked by remember { mutableStateOf(DatabaseProvider.isBookmarked(recipe.url)) }

    val defaultImageUrl = "https://github.com/YuRoGameDev/EzEats/blob/main/icons/home.png"

    val title = recipe.title.takeIf { it.isNotEmpty() } ?: "Title Unavailable"
    val imageUrl = recipe.imageUrl.takeIf { it.isNotEmpty() } ?: defaultImageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
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
            //Image data. If no image is found use placeholder
            val imagePainter = rememberAsyncImagePainter(
                model = imageUrl,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder),
            )
            val imageState = imagePainter.state

            //Composes the card Thumbnail
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (imageState is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(30.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Image(
                    painter = imagePainter,
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            //The actual recipe info
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(title, style = MaterialTheme.typography.titleMedium)

                //Time it takes and Rating
                val displayRating = recipe.rating?.let { "⭐ $it" } ?: "⭐ N/A"
                Text(
                    "Time: ${recipe.time} - $displayRating",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Ingredient List
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

                // Bookmark and Webview buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //Web View
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
                            imageVector = Icons.Default.Menu,
                            contentDescription = "View",
                            modifier = Modifier.size(52.dp)

                        )
                    }
                    //Book mark
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
                            modifier = Modifier.size(52.dp),
                        )
                    }
                }
            }
        }
    }
}



