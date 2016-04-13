package com.example.magnus.livingthing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class VideoViewActivity extends AppCompatActivity {

    private WebView videoPlayer = null;
    private final String LOG_TAG = VideoViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "On resume");

        SharedPreferences prefs = getSharedPreferences(getString(R.string.content_authority), Context.MODE_PRIVATE);
        String urlStr = prefs.getString(getString(R.string.pref_raspberry_pi_url), getString(R.string.pref_raspberry_pi_url_default));

        videoPlayer = new WebView(this);
        setContentView(videoPlayer);
        videoPlayer.loadUrl("http://" + urlStr + "/videoplayer");
        videoPlayer.setVisibility(View.GONE);
        videoPlayer.setWebViewClient(new WebViewClient() {
            private boolean error;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                super.onPageStarted(view, url, favicon);
                error = false;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                error = true;
                Log.e("HTTP Error 404 Webview", errorResponse.toString());
            }



            @Override
            public void onPageFinished(WebView view, String url) {

                if (!error) {
                    view.setVisibility( View.VISIBLE );
                }
                error = false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.destroy();
    }
}