package com.maersk.fbm.rdt_mobile.bridge;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.content.Context;

import com.maersk.fbm.rdt_mobile.R;
import com.maersk.fbm.rdt_mobile.utils.BeepController;
import com.maersk.fbm.rdt_mobile.utils.CompletionHandler;
import com.maersk.fbm.rdt_mobile.utils.MessageEvent;
import com.maersk.fbm.rdt_mobile.utils.SharedPreferenceUtil;

import org.greenrobot.eventbus.EventBus;

public class JsApi {
    private final Context context;
    private BeepController mBeepController;

    public JsApi(Context c) {
        context = c;
        mBeepController = new BeepController(c);
    }

    @JavascriptInterface
    public String enableCamera(Object msg) {
        if (msg.toString().equals(context.getString(R.string.enable_camera))) {
            EventBus.getDefault().post(new MessageEvent(context.getString(R.string.image_capture_activity), "dummy data"));
            return "SUCCESS";
        }
        return "FAIL";
    }

    @JavascriptInterface
    public void getDevice(Object arg, CompletionHandler<String> handler) {
        handler.complete("CT45");
    }

    @JavascriptInterface
    public String beep(Object arg) {
        // beep fail sound
        mBeepController.beep(false);
        return "OK";
    }

}
