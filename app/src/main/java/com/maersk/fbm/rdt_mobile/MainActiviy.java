package com.maersk.fbm.rdt_mobile;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebSettings;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.maersk.fbm.rdt_mobile.utils.DWebView;

public class MainActiviy extends AppCompatActivity {

    DWebView dWebView;

    public <T extends View> T getView(int viewId) {
        View view = findViewById(viewId);
        return (T) view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dWebView = getView(R.id.webview);
        dWebView.loadUrl("http://10.4.1.90:8080/");
        DWebView.setWebContentsDebuggingEnabled(true);
        dWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        // fullscreen by code
        hide();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHidePart2Runnable.run();
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                getWindow().getDecorView().getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
}
