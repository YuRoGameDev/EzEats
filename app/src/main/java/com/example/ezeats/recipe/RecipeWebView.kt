package com.example.ezeats.recipe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.IconButton

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.ezeats.DatabaseProvider
import com.example.ezeats.R


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RecipeWebView(
    url: String,
    onBack: () -> Unit
) {
    var webView: WebView? = remember { null }
    var isBookmarked by remember { mutableStateOf(DatabaseProvider.isBookmarked(url)) }
    var isWebViewVisible by remember { mutableStateOf(false) }

    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onBack()
        }
    }

    val context = LocalContext.current

    LaunchedEffect(key1 = url) {
        isWebViewVisible = true // Make WebView visible when launched
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {

        AnimatedVisibility(
            visible = isWebViewVisible,
            enter = slideInVertically(initialOffsetY = { it }), // Slide in from bottom
            exit = slideOutVertically(targetOffsetY = { it }) // Optionally slide out
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest
                            ): WebResourceResponse? {
                                val reqUrl = request.url.toString().lowercase(Locale.ROOT)
                                return if (
                                    reqUrl.contains("doubleclick") ||
                                    reqUrl.contains("ads.") ||
                                    reqUrl.contains("googlesyndication") ||
                                    reqUrl.contains("adservice")
                                ) {
                                    WebResourceResponse("text/plain", "utf-8", null)
                                } else {
                                    super.shouldInterceptRequest(view, request)
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest
                            ): Boolean {
                                view?.loadUrl(request.url.toString())
                                return true
                            }
                        }

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.setSupportMultipleWindows(false)
                        settings.javaScriptCanOpenWindowsAutomatically = false

                        loadUrl(url)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )


        // Close button (Top-left)

        }
        // Bookmark + Share buttons (Top-right)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            IconButton(
                onClick = {
                    if(DatabaseProvider.isBookmarked(url)){
                        DatabaseProvider.removeBookmark(url)
                    }else{
                        DatabaseProvider.addBookmark(url)
                    }
                    isBookmarked = !isBookmarked
                },
                modifier = Modifier
                    .background(Color(0xBF9dc484), shape = CircleShape)
                    .size(50.dp)
            ) {
                Image(
                    painter = painterResource(id = if (isBookmarked) R.drawable.bookmarked else R.drawable.bookmark_none),
                    contentDescription = "Bookmark",
                    modifier = Modifier.size(42.dp), // Adjust size as needed

                )
            }
            IconButton(
                onClick = { shareRecipe(context,url) },
                modifier = Modifier
                    .background(Color(0xBF9dc484), shape = CircleShape)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(
                onClick = { onBack() },
                modifier = Modifier
                    .background(Color(0xBF9dc484), shape = CircleShape)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

    }
}


fun shareRecipe(context: Context, recipeUrl: String) {

    // Using LaunchedEffect to perform side effects (starting intent) safely in Composable context
    val message = "Check out this awesome recipe: $recipeUrl"
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)

        // Check if context is an instance of Activity, apply the flag to start the activity
        if (context is Activity) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    // Start the activity using the context
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}

