package com.bhai.vault
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Android 16 - Permission mango
        askPermissions()
        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        // ===== ASLI WEBVIEW SETTING - Yahi sabse important hai =====
        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.allowFileAccess = true
        s.allowContentAccess = true
        s.mediaPlaybackRequiresUserGesture = false
        s.javaScriptCanOpenWindowsAutomatically = true
        s.setSupportMultipleWindows(true)
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.userAgentString = "Mozilla/5.0 (Linux; Android 16; Mobile) Chrome/130 BhaiVault"
        WebView.setWebContentsDebuggingEnabled(true)
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, req: WebResourceRequest): Boolean { return false } // sab kuch isi WebView me khulega
            override fun onReceivedError(v: WebView, req: WebResourceRequest, err: WebResourceError) { if(req.isForMainFrame) v.loadUrl("file:///android_asset/error.html") }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(w: WebView, cb: ValueCallback<Array<Uri>>, p: FileChooserParams): Boolean {
                // Yaha file picker khulega - Gallery / Files ki permission se
                fileCallback = cb
                filePicker.launch(p.createIntent())
                return true
            }
        }
        // Vault ka HTML assets me se load hoga
        // Pehle Google kholo, VAULT button se vault khulega
        loadUrl("https://www.google.com")
        findViewById<Button>(R.id.btnGo).setOnClickListener { var u = urlBar.text.toString().trim(); if(!u.startsWith("http")) u = "https://$u"; loadUrl(u) }
        findViewById<Button>(R.id.btnBack).setOnClickListener { if(webView.canGoBack()) webView.goBack() }
        findViewById<Button>(R.id.btnVault).setOnClickListener { webView.loadUrl("file:///android_asset/vault.html") }
    }
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private val filePicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { res ->
        val uris = if(res.data?.data != null) arrayOf(res.data!!.data!!) else emptyArray()
        fileCallback?.onReceiveValue(uris); fileCallback = null
    }
    private fun loadUrl(u: String) { urlBar.setText(u); webView.loadUrl(u) }
    private fun askPermissions() {
        val perms = mutableListOf<String>()
        if(Build.VERSION.SDK_INT >= 33) { perms.add(Manifest.permission.READ_MEDIA_IMAGES); perms.add(Manifest.permission.READ_MEDIA_VIDEO); perms.add(Manifest.permission.READ_MEDIA_AUDIO) }
        else { perms.add(Manifest.permission.READ_EXTERNAL_STORAGE) }
        perms.add(Manifest.permission.CAMERA)
        val need = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if(need.isNotEmpty()) ActivityCompat.requestPermissions(this, need.toTypedArray(), 100)
    }
    override fun onBackPressed() { if(webView.canGoBack()) webView.goBack() else super.onBackPressed() }
}
