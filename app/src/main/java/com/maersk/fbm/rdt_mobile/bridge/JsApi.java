package com.maersk.fbm.rdt_mobile.bridge;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.content.Context;

import com.maersk.fbm.rdt_mobile.R;
import com.maersk.fbm.rdt_mobile.utils.CompletionHandler;
import com.maersk.fbm.rdt_mobile.utils.MessageEvent;

import org.greenrobot.eventbus.EventBus;

public class JsApi {
    private final Context context;

    public JsApi(Context c) {
        context = c;
    }

    @JavascriptInterface
    public String enableCamera(Object msg) {
        if (msg.toString().equals(context.getString(R.string.enable_camera))) {
            EventBus.getDefault().post(new MessageEvent(context.getString(R.string.image_capture_activity), "dummy data"));
            return "SUCCESS";
        }
        return "FAIL";
    }

//    @JavascriptInterface
//    public void testAsyn(Object msg, CompletionHandler handler) {
//        handler.complete(msg+" [ asyn call]");
//    }
}
