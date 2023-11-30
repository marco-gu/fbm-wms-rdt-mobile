package com.maersk.fbm.rdt_mobile;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;

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
    }
}
