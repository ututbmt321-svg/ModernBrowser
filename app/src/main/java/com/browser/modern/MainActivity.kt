package com.browser.modern

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

data class BrowserProfile(val name: String, val home: String = "https://www.google.com")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var profiles by remember { mutableStateOf(listOf(BrowserProfile("Personal"))) }
                var currentProfile by remember { mutableIntStateOf(0) }
                var showProfiles by remember { mutableStateOf(false) }
                var address by remember { mutableStateOf("https://www.google.com") }
                var requestedUrl by remember { mutableStateOf(address) }
                var webView by remember { mutableStateOf<WebView?>(null) }
                var canBack by remember { mutableStateOf(false) }
                var canForward by remember { mutableStateOf(false) }
                var showAdd by remember { mutableStateOf(false) }
                var newName by remember { mutableStateOf("") }

                fun navigate(input: String) {
                    val value = input.trim()
                    if (value.isEmpty()) return
                    requestedUrl = if (value.startsWith("https://") || value.startsWith("http://")) value
                    else if (value.contains(".") && !value.contains(" ")) "https://$value"
                    else "https://www.google.com/search?q=" + java.net.URLEncoder.encode(value, "UTF-8")
                }

                BackHandler(canBack) { webView?.goBack() }
                Scaffold(
                    topBar = {
                        Column {
                            Row {
                                TextButton(onClick = { showProfiles = true }) {
                                    Text(profiles[currentProfile].name)
                                }
                                Spacer(Modifier.weight(1f))
                                Text("ModernBrowser")
                            }
                            Row {
                                TextField(
                                    address, { address = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Địa chỉ hoặc tìm kiếm") }
                                )
                                Button(onClick = { navigate(address) }) { Text("Đi") }
                            }
                        }
                    },
                    bottomBar = {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(onClick = { webView?.goBack() }, enabled = canBack) { Text("←") }
                            TextButton(onClick = { webView?.goForward() }, enabled = canForward) { Text("→") }
                            TextButton(onClick = { navigate(profiles[currentProfile].home) }) { Text("Trang chủ") }
                            TextButton(onClick = { webView?.reload() }) { Text("⟳") }
                        }
                    }
                ) { padding ->
                    AndroidView(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        address = url ?: address
                                        canBack = view?.canGoBack() == true
                                        canForward = view?.canGoForward() == true
                                    }
                                }
                                loadUrl(requestedUrl)
                                webView = this
                            }
                        },
                        update = { view ->
                            if (view.url != requestedUrl && view.originalUrl != requestedUrl) {
                                view.loadUrl(requestedUrl)
                            }
                        }
                    )
                }
                if (showProfiles) AlertDialog(
                    onDismissRequest = { showProfiles = false },
                    title = { Text("Hồ sơ") },
                    text = {
                        Column {
                            profiles.forEachIndexed { index, profile ->
                                TextButton(onClick = {
                                    currentProfile = index
                                    showProfiles = false
                                    navigate(profile.home)
                                }) {
                                    Text(profile.name + if (index == currentProfile) " ✓" else "")
                                }
                            }
                            TextButton(onClick = {
                                showProfiles = false
                                showAdd = true
                            }) { Text("+ Thêm hồ sơ") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showProfiles = false }) { Text("Đóng") }
                    }
                )
                if (showAdd) AlertDialog(
                    onDismissRequest = { showAdd = false },
                    title = { Text("Thêm hồ sơ") },
                    text = {
                        TextField(newName, { newName = it }, label = { Text("Tên hồ sơ") })
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newName.isNotBlank()) {
                                profiles = profiles + BrowserProfile(newName.trim())
                                currentProfile = profiles.lastIndex
                                navigate(profiles[currentProfile].home)
                            }
                            newName = ""
                            showAdd = false
                        }) { Text("Thêm") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAdd = false }) { Text("Hủy") }
                    }
                )
            }
        }
    }
}
