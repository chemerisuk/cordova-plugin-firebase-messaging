package by.chemerisuk.cordova.firebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationManagerCompat;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

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


public class FirebaseMessagingPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FirebaseMessagingPlugin";

    private CallbackContext instanceIdCallback;
    private CallbackContext foregroundCallback;
    private CallbackContext backgroundCallback;
    private static JSONObject lastBundle;
    private static FirebaseMessagingPlugin instance;

    @Override
    protected void pluginInitialize() {
        FirebaseMessagingPlugin.instance = this;

        Context context = cordova.getActivity().getApplicationContext();
        Bundle bundle = cordova.getActivity().getIntent().getExtras();
        try {
            lastBundle = getNotificationData(bundle);
        } catch (JSONException e) {
            Log.e(TAG, "pluginInitialize", e);
        }
        // cleanup badge value initially
        ShortcutBadger.applyCount(context, 0);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            JSONObject notificationData = getNotificationData(intent.getExtras());
            if (notificationData != null) {
                sendNotification(notificationData, true);
            }
        } catch (JSONException e) {
            Log.e(TAG, "onNewIntent", e);
        }
    }

    @CordovaMethod
    private void subscribe(String topic, CallbackContext callbackContext) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);

        callbackContext.success();
    }

    @CordovaMethod
    private void unsubscribe(String topic, CallbackContext callbackContext) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);

        callbackContext.success();
    }

    @CordovaMethod
    private void getToken(CallbackContext callbackContext) {
        String token = FirebaseInstanceId.getInstance().getToken();

        callbackContext.success(token);
    }

    @CordovaMethod
    private void onTokenRefresh(CallbackContext callbackContext) {
        instance.instanceIdCallback = callbackContext;
    }

    @CordovaMethod
    private void onMessage(CallbackContext callbackContext) {
        instance.foregroundCallback = callbackContext;
    }

    @CordovaMethod
    private void onBackgroundMessage(CallbackContext callbackContext) {
        instance.backgroundCallback = callbackContext;

        if (lastBundle != null) {
            sendNotification(lastBundle, true);
            lastBundle = null;
        }
    }

    @CordovaMethod
    private void setBadge(int value, CallbackContext callbackContext) {
        if (value >= 0) {
            Context context = cordova.getActivity().getApplicationContext();
            ShortcutBadger.applyCount(context, value);

            callbackContext.success();
        } else {
            callbackContext.error("Badge value can't be negative");
        }
    }

    @CordovaMethod
    private void getBadge(CallbackContext callbackContext) {
        Context context = cordova.getActivity();
        SharedPreferences settings = context.getSharedPreferences("badge", Context.MODE_PRIVATE);
        int number = settings.getInt("badge", 0);
        callbackContext.success(number);
    }

    @CordovaMethod
    private void requestPermission(CallbackContext callbackContext) {
        Context context = cordova.getActivity().getApplicationContext();
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            callbackContext.success();
        } else {
            callbackContext.error("Push notifications are disabled");
        }
    }

    public static void sendNotification(JSONObject notificationData, boolean background) {
        if (instance != null) {
            CallbackContext callback = background ? instance.backgroundCallback : instance.foregroundCallback;
            if (callback != null) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, notificationData);
                pluginResult.setKeepCallback(true);
                callback.sendPluginResult(pluginResult);
            }
        }
    }

    public static void sendInstanceId(String instanceId) {
        if (instance != null && instance.instanceIdCallback != null && instanceId != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, instanceId);
            pluginResult.setKeepCallback(true);
            instance.instanceIdCallback.sendPluginResult(pluginResult);
        }
    }

    private static JSONObject getNotificationData(Bundle bundle) throws JSONException {
        if (bundle == null) {
            return null;
        }

        if (!bundle.containsKey("google.message_id") && !bundle.containsKey("google.sent_time")) {
            return null;
        }

        JSONObject notificationData = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            notificationData.put(key, bundle.get(key));
        }
        return notificationData;
    }
}
