package com.example.ezeats.recipe

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.*
import java.util.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RecipeWebView(url: String, onBack: () -> Unit) {
    var webView: WebView? = remember { null }

    // Handle Android back button to go back in webview or exit
    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onBack()
        }
    }

    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val url = request.url.toString().lowercase(Locale.ROOT)
                    // 🚫 Block requests to common ad servers
                    return if (url.contains("doubleclick") ||
                        url.contains("ads.") ||
                        url.contains("googlesyndication") ||
                        url.contains("adservice")) {
                        WebResourceResponse("text/plain", "utf-8", null)
                    } else {
                        super.shouldInterceptRequest(view, request)
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest
                ): Boolean {
                    // Prevent popups trying to open in a new window
                    view?.loadUrl(request.url.toString())
                    return true
                }
            }

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(false) // 🔒 Block popups
            settings.javaScriptCanOpenWindowsAutomatically = false

            loadUrl(url)
            webView = this
        }
    })
}