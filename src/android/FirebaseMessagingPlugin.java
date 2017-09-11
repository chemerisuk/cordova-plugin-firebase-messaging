package by.chemerisuk.cordova.firebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import me.leolin.shortcutbadger.ShortcutBadger;


public class FirebaseMessagingPlugin extends CordovaPlugin {
    private static final String TAG = "FirebaseMessagingPlugin";

    private CallbackContext tokenCallback;
    private CallbackContext notificationCallback;
    private Bundle lastBundle;
    private static FirebaseMessagingPlugin instance;

    @Override
    protected void pluginInitialize() {
        FirebaseMessagingPlugin.instance = this;

        Context context = cordova.getActivity().getApplicationContext();
        Bundle bundle = cordova.getActivity().getIntent().getExtras();
        if (bundle != null && (bundle.containsKey("google.message_id") || bundle.containsKey("google.sent_time"))) {
            lastBundle = bundle;
        }
        // cleanup badge value initially
        ShortcutBadger.applyCount(context, 0);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("subscribe".equals(action)) {
            this.subscribe(callbackContext, args.getString(0));
            return true;
        } else if ("unsubscribe".equals(action)) {
            this.unsubscribe(callbackContext, args.getString(0));
            return true;
        } else if ("onTokenRefresh".equals(action)) {
            this.registerTokenReceiver(callbackContext);
            return true;
        } else if ("onMessage".equals(action)) {
            this.registerMessageReceiver(callbackContext);
            return true;
        } else if ("setBadge".equals(action)) {
            this.setBadge(callbackContext, args.optInt(0));
            return true;
        } else if ("getBadge".equals(action)) {
            this.getBadge(callbackContext);
            return true;
        } else if ("requestPermission".equals(action)) {
            this.requestPermission(callbackContext);
            return true;
        }

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            sendNotification(getNotification(intent.getExtras()));
        } catch (JSONException e) {
            Log.e(TAG, "onNewIntent", e);
        }
    }

    private void subscribe(CallbackContext callbackContext, String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);

        callbackContext.success();
    }

    private void unsubscribe(CallbackContext callbackContext, String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);

        callbackContext.success();
    }

    private void registerTokenReceiver(CallbackContext callbackContext) {
        String token = FirebaseInstanceId.getInstance().getToken();

        instance.tokenCallback = callbackContext;

        sendToken(token);
    }

    private void registerMessageReceiver(CallbackContext callbackContext) throws JSONException {
        instance.notificationCallback = callbackContext;

        if (instance.lastBundle != null) {
            sendNotification(getNotification(instance.lastBundle));
            instance.lastBundle = null;
        }
    }

    private void setBadge(CallbackContext callbackContext, int value) {
        if (value >= 0) {
            Context context = cordova.getActivity();
            ShortcutBadger.applyCount(context.getApplicationContext(), value);

            callbackContext.success();
        } else {
            callbackContext.error("Badge value can't be negative");
        }
    }

    private void getBadge(CallbackContext callbackContext) {
        Context context = cordova.getActivity();
        SharedPreferences settings = context.getSharedPreferences("badge", Context.MODE_PRIVATE);
        int number = settings.getInt("badge", 0);
        callbackContext.success(number);
    }

    private void requestPermission(CallbackContext callbackContext) {
        callbackContext.success();
    }

    public static void sendNotification(JSONObject notificationData) throws JSONException {
        if (instance.notificationCallback != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, notificationData);
            pluginResult.setKeepCallback(true);
            instance.notificationCallback.sendPluginResult(pluginResult);
        }
    }

    public static void sendToken(String token) {
        if (instance.tokenCallback != null && token != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, token);
            pluginResult.setKeepCallback(true);
            instance.tokenCallback.success(pluginResult);
        }
    }

    private static JSONObject getNotification(Bundle bundle) throws JSONException {
        JSONObject notificationData = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            notificationData.put(key, bundle.get(key));
        }

        notificationData.put("background", 1);

        return notificationData;
    }
}
