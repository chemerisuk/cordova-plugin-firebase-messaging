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

    private static CallbackContext tokenCallback;
    private static CallbackContext notificationCallback;
    private static Bundle lastBundle;

    @Override
    protected void pluginInitialize() {
        Context context = this.cordova.getActivity().getApplicationContext();
        Bundle bundle = this.cordova.getActivity().getIntent().getExtras();
        if (bundle != null && (bundle.containsKey("google.message_id") || bundle.containsKey("google.sent_time"))) {
            lastBundle = bundle;
        }

        FirebaseMessaging.getInstance().subscribeToTopic("android");
        FirebaseMessaging.getInstance().subscribeToTopic("all");
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

        sendNotification(intent.getExtras());
    }

    public static void sendNotification(RemoteMessage remoteMessage) {
        if (remoteMessage != null && FirebaseMessagingPlugin.notificationCallback != null) {
            JSONObject json = new JSONObject(remoteMessage.getData());
            RemoteMessage.Notification notification = remoteMessage.getNotification();

            try {
                if (notification != null) {
                    JSONObject jsonNotification = new JSONObject();

                    jsonNotification.put("body", notification.getBody());
                    jsonNotification.put("title", notification.getTitle());
                    jsonNotification.put("sound", notification.getSound());
                    jsonNotification.put("icon", notification.getIcon());
                    jsonNotification.put("tag", notification.getTag());
                    jsonNotification.put("color", notification.getColor());
                    jsonNotification.put("clickAction", notification.getClickAction());

                    json.put("gcm", jsonNotification);
                }

                json.put("google.message_id", remoteMessage.getMessageId());
                json.put("google.sent_time", remoteMessage.getSentTime());
                json.put("background", 0);

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                pluginResult.setKeepCallback(true);
                FirebaseMessagingPlugin.notificationCallback.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e(TAG, "Fail to handle notification", e);
            }
        }
    }

    public static void sendNotification(Bundle bundle) {
        if (bundle != null && FirebaseMessagingPlugin.notificationCallback != null) {
            JSONObject json = new JSONObject();
            Set<String> keys = bundle.keySet();

            try {
                for (String key : keys) {
                    json.put(key, bundle.get(key));
                }

                json.put("background", 1);

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                pluginResult.setKeepCallback(true);
                FirebaseMessagingPlugin.notificationCallback.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e(TAG, "Fail to handle notification", e);
            }
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

        FirebaseMessagingPlugin.tokenCallback = callbackContext;

        if (token != null) {
            FirebaseMessagingPlugin.sendToken(token);
        }
    }

    private void registerMessageReceiver(CallbackContext callbackContext) {
        FirebaseMessagingPlugin.notificationCallback = callbackContext;

        if (FirebaseMessagingPlugin.lastBundle != null) {
            sendNotification(FirebaseMessagingPlugin.lastBundle);

            FirebaseMessagingPlugin.lastBundle = null;
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

    public static void sendToken(String token) {
        if (FirebaseMessagingPlugin.tokenCallback != null) {
            FirebaseMessagingPlugin.tokenCallback.success(token);

            FirebaseMessagingPlugin.tokenCallback = null;
        }
    }
}
