package by.chemerisuk.cordova.firebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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


public class FirebaseMessagingPlugin extends CordovaPlugin {
    private static final String TAG = "FirebaseMessagingPlugin";

    private static CallbackContext tokenCallback;
    private static CallbackContext notificationCallback;
    private static Bundle notificationBundle;

    @Override
    protected void pluginInitialize() {
        Context context = this.cordova.getActivity().getApplicationContext();
        Bundle bundle = this.cordova.getActivity().getIntent().getExtras();
        if (bundle != null && (bundle.containsKey("google.message_id") || bundle.containsKey("google.sent_time"))) {
            notificationBundle = bundle;
        }

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
        } else if ("handleNotification".equals(action)) {
            FirebaseMessagingPlugin.notificationCallback = callbackContext;

            if (FirebaseMessagingPlugin.notificationBundle != null) {
                handleNotification(FirebaseMessagingPlugin.notificationBundle);

                FirebaseMessagingPlugin.notificationBundle = null;
            }
            return true;
        }

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleNotification(intent.getExtras());
    }

    public static void handleNotification(RemoteMessage remoteMessage) {
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

                    json.put("notification", jsonNotification);
                }

                json.put("google.message_id", remoteMessage.getMessageId());
                json.put("google.sent_time", remoteMessage.getSentTime());

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                pluginResult.setKeepCallback(true);
                FirebaseMessagingPlugin.notificationCallback.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e(TAG, "Fail to handle notification", e);
            }
        }
    }

    public static void handleNotification(Bundle bundle) {
        if (bundle != null && FirebaseMessagingPlugin.notificationCallback != null) {
            JSONObject json = new JSONObject();
            Set<String> keys = bundle.keySet();

            try {
                for (String key : keys) {
                    json.put(key, bundle.get(key));
                }

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
