package by.chemerisuk.cordova.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;


public class FirebaseMessagingPlugin extends CordovaPlugin {
    private static final String TAG = "FirebaseMessagingPlugin";

    private static CallbackContext tokenCallback;

    @Override
    protected void pluginInitialize() {
        FirebaseMessaging.getInstance().subscribeToTopic("android");
        FirebaseMessaging.getInstance().subscribeToTopic("all");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("subscribe".equals(action)) {
            this.subscribe(callbackContext, args.getString(0));
            return true;
        } else if ("unsubscribe".equals(action)) {
            this.unsubscribe(callbackContext, args.getString(0));
            return true;
        } else if ("getDeviceToken".equals(action)) {
            this.readDeviceToken(callbackContext);
            return true;
        }

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        FirebaseMessagingPlugin.onNotificationOpen(intent.getExtras());
    }

    public static void onNotificationOpen(Bundle bundle) {
        Log.d(TAG, "onNotificationOpen" + bundle);
        // if (callbackContext != null && bundle != null) {
        //     JSONObject json = new JSONObject();
        //     Set<String> keys = bundle.keySet();
        //     for (String key : keys) {
        //         try {
        //             json.put(key, bundle.get(key));
        //         } catch (JSONException e) {
        //             callbackContext.error(e.getMessage());
        //             return;
        //         }
        //     }

        //     callbackContext.success(json);
        // }
    }


    private void subscribe(CallbackContext callbackContext, String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);

        callbackContext.success();
    }

    private void unsubscribe(CallbackContext callbackContext, String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);

        callbackContext.success();
    }

    private void readDeviceToken(CallbackContext callbackContext) {
        String token = FirebaseInstanceId.getInstance().getToken();

        FirebaseMessagingPlugin.tokenCallback = callbackContext;

        if (token != null) {
            sendDeviceToken(token);
        }
    }

    public static void sendDeviceToken(String token) {
        if (FirebaseMessagingPlugin.tokenCallback != null) {
            FirebaseMessagingPlugin.tokenCallback.success(token);

            FirebaseMessagingPlugin.tokenCallback = null;
        }
    }
}
