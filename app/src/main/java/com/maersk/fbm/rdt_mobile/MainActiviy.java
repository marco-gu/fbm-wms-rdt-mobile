package com.maersk.fbm.rdt_mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebSettings;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.maersk.fbm.rdt_mobile.utils.DWebView;
import com.maersk.fbm.rdt_mobile.bridge.JsApi;
import com.maersk.fbm.rdt_mobile.utils.MessageEvent;
import com.maersk.fbm.rdt_mobile.views.ImageCaptureActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;


public class MainActiviy extends AppCompatActivity {

    DWebView dWebView;
    private Map<Integer, String> keycodeMap;

    public <T extends View> T getView(int viewId) {
        View view = findViewById(viewId);
        return (T) view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dWebView = getView(R.id.webview);
        dWebView.loadUrl("http://172.16.30.66:8081/");

        DWebView.setWebContentsDebuggingEnabled(true);
        dWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        dWebView.addJavascriptObject(new JsApi(this), null);
    }

//    private void hide() {
//        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        mHidePart2Runnable.run();
//    }

//    private final Runnable mHidePart2Runnable = new Runnable() {
//        @SuppressLint("InlinedApi")
//        @Override
//        public void run() {
//            // removal of status and navigation bar
//            if (Build.VERSION.SDK_INT >= 30) {
////                getWindow().getDecorView().getWindowInsetsController().hide(
////                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
//                getWindow().getDecorView().getWindowInsetsController().hide(WindowInsets.Type.navigationBars());
//            } else {
//                // Note that some of these constants are new as of API 16 (Jelly Bean)
//                // and API 19 (KitKat). It is safe to use them, as they are inlined
//                // at compile-time and do nothing on earlier devices.
////                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
////                        | View.SYSTEM_UI_FLAG_FULLSCREEN
////                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
////                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
////                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
////                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//                getWindow().getDecorView().setSystemUiVisibility(
//                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
//                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//            }
//        }
//    };

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe()
    public void onEventMainThread(MessageEvent messageEvent) {
        if (messageEvent.type.equals(this.getString(R.string.image_capture_activity))) {
            Intent intent = new Intent(getApplicationContext(), ImageCaptureActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case 4:
                    dWebView.callHandler("onBackPressed", new Object[]{});
                    return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
