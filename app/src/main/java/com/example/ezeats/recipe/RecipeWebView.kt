package com.example.ezeats.recipe

import android.annotation.SuppressLint
import android.app.Activity

import android.content.Context
import android.content.Intent

import android.webkit.WebView
import android.webkit.WebViewClient

import android.webkit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.BackHandler

import androidx.compose.animation.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.*

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.R
import com.example.ezeats.ui.theme.transLightGreen

//This handles the actual Web Viewer
//It basically opens a browser within the app to view the recipe
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
        isWebViewVisible = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {

        AnimatedVisibility(
            visible = isWebViewVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            //The android view. One thing added was removing all the popup ads
            //Interactivity was limited to just the simple clicking and scrolling
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

        }

        //These are the custom Buttons included with the WebView
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //Book marking
            IconButton(
                onClick = {
                    if (DatabaseProvider.isBookmarked(url)) {
                        DatabaseProvider.removeBookmark(url)
                    } else {
                        DatabaseProvider.addBookmark(url)
                    }
                    isBookmarked = !isBookmarked
                },
                modifier = Modifier
                    .background(transLightGreen, shape = CircleShape)
                    .size(50.dp)
            ) {
                Image(
                    painter = painterResource(id = if (isBookmarked) R.drawable.bookmarked else R.drawable.bookmark_none),
                    contentDescription = "Bookmark",
                    modifier = Modifier.size(42.dp),
                )
            }
            //Recipe Share
            IconButton(
                onClick = { shareRecipe(context, url) },
                modifier = Modifier
                    .background(transLightGreen, shape = CircleShape)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
            //Close the Webview
            IconButton(
                onClick = { onBack() },
                modifier = Modifier
                    .background(transLightGreen, shape = CircleShape)
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

//Allows users to share recipes through the messaging app
fun shareRecipe(context: Context, recipeUrl: String) {
    val message = "Check out this awesome recipe: $recipeUrl"
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)

        if (context is Activity) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}

