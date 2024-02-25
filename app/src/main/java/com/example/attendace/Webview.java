package com.example.attendace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class Webview extends AppCompatActivity {

    private WebView webView;
    private static final String PREF_IS_SIGNED_IN = "is_signed_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webView);
        configureWebView();

        String url = getIntent().getStringExtra("url");

        if (requiresGoogleSignIn(url) && !isSignedIn()) {
            signInWithGoogle();
            return;
        }

        loadUrl(url);
    }

    // Check if the URL requires Google Sign-In
    private boolean requiresGoogleSignIn(String url) {
        return url.contains("https://accounts.google.com/");
    }

    // Check if the user is signed in
    private boolean isSignedIn() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return preferences.getBoolean(PREF_IS_SIGNED_IN, false);
    }

    private void configureWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
    }

    private void loadUrl(String url) {
        webView.loadUrl(url);
    }

    // Implement your sign-in logic
    private void signInWithGoogle() {
        // Handle the sign-in process and set the isSignedIn state accordingly.
        // After a successful sign-in, you can load the WebView with the desired URL.
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // If the WebView cannot go back, start the Internal.class
            startActivity(new Intent(this, Internal.class));
            finish(); // Finish the Webview activity to prevent it from being retained in the back stack
        }
    }
}
