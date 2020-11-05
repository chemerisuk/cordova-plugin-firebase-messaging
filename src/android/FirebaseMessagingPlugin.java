package by.chemerisuk.cordova.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;
import me.leolin.shortcutbadger.ShortcutBadger;

import static androidx.core.content.ContextCompat.getSystemService;


public class FirebaseMessagingPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FCMPlugin";

    private JSONObject lastBundle;
    private boolean isBackground = false;
    private boolean forceShow = false;
    private CallbackContext tokenRefreshCallback;
    private CallbackContext foregroundCallback;
    private CallbackContext backgroundCallback;
    private static FirebaseMessagingPlugin instance;
    private NotificationManager notificationManager;
    private FirebaseMessaging firebaseMessaging;

    @Override
    protected void pluginInitialize() {
        FirebaseMessagingPlugin.instance = this;

        firebaseMessaging = FirebaseMessaging.getInstance();
        notificationManager = getSystemService(cordova.getActivity(), NotificationManager.class);
        lastBundle = getNotificationData(cordova.getActivity().getIntent());
    }

    @CordovaMethod
    private void subscribe(String topic, final CallbackContext callbackContext) {
        firebaseMessaging.subscribeToTopic(topic).addOnCompleteListener(cordova.getActivity(), task -> {
            if (task.isSuccessful()) {
                callbackContext.success();
            } else {
                callbackContext.error(task.getException().getMessage());
            }
        });
    }

    @CordovaMethod
    private void unsubscribe(String topic, final CallbackContext callbackContext) {
        firebaseMessaging.unsubscribeFromTopic(topic).addOnCompleteListener(cordova.getActivity(), task -> {
            if (task.isSuccessful()) {
                callbackContext.success();
            } else {
                callbackContext.error(task.getException().getMessage());
            }
        });
    }

    @CordovaMethod
    private void clearNotifications(CallbackContext callbackContext) {
        notificationManager.cancelAll();

        callbackContext.success();
    }

    @CordovaMethod
    private void deleteToken(CallbackContext callbackContext) {
        firebaseMessaging.deleteToken().addOnCompleteListener(cordova.getActivity(), task -> {
            if (task.isSuccessful()) {
                callbackContext.success();
            } else {
                callbackContext.error(task.getException().getMessage());
            }
        });
    }

    @CordovaMethod
    private void getToken(String type, final CallbackContext callbackContext) {
        if (type.isEmpty()) {
            firebaseMessaging.getToken().addOnCompleteListener(cordova.getActivity(), task -> {
                if (task.isSuccessful()) {
                    callbackContext.success(task.getResult());
                } else {
                    callbackContext.error(task.getException().getMessage());
                }
            });
        } else {
            callbackContext.sendPluginResult(
                new PluginResult(PluginResult.Status.OK, (String)null));
        }
    }

    @CordovaMethod
    private void onTokenRefresh(CallbackContext callbackContext) {
        instance.tokenRefreshCallback = callbackContext;
    }

    @CordovaMethod
    private void onMessage(CallbackContext callbackContext) {
        instance.foregroundCallback = callbackContext;
    }

    @CordovaMethod
    private void onBackgroundMessage(CallbackContext callbackContext) {
        instance.backgroundCallback = callbackContext;

        if (lastBundle != null) {
            sendNotification(lastBundle, callbackContext);
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
        callbackContext.success(settings.getInt("badge", 0));
    }

    @CordovaMethod
    private void requestPermission(JSONObject options, CallbackContext callbackContext) {
        Context context = cordova.getActivity().getApplicationContext();

        this.forceShow = options.optBoolean("forceShow");

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            callbackContext.success();
        } else {
            callbackContext.error("Notifications permission is not granted");
        }
    }

    @CordovaMethod
    private void createChannel(JSONObject options, CallbackContext callbackContext) throws JSONException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Notification channels are not supported");
        }

        String channelId = options.getString("id");
        String channelName = options.getString("name");
        int importance = options.getInt("importance");
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(options.optString("description", ""));
        channel.setShowBadge(options.optBoolean("badge", true));

        channel.enableLights(options.optBoolean("light", false));
        channel.setLightColor(options.optInt("lightColor", 0));

        String soundName = options.optString("sound", "default");
        if (!"default".equals(soundName)) {
            String packageName = cordova.getActivity().getPackageName();
            Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/" + soundName);
            channel.setSound(soundUri, new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build());
        }

        JSONArray vibrationPattern = options.optJSONArray("vibration");
        if (vibrationPattern != null) {
            int patternLength = vibrationPattern.length();
            long[] patternArray = new long[patternLength];
            for (int i = 0; i < patternLength; i++) {
                patternArray[i] = vibrationPattern.getLong(i);
            }
            channel.setVibrationPattern(patternArray);
            channel.enableVibration(true);
        } else {
            channel.enableVibration(options.optBoolean("vibration", true));
        }

        notificationManager.createNotificationChannel(channel);

        callbackContext.success();
    }

    @CordovaMethod
    private void findChannel(String channelId, CallbackContext callbackContext) throws JSONException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Notification channels are not supported");
        }

        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
        if (channel == null) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, (String)null));
        } else {
            callbackContext.success(new JSONObject()
                .put("id", channel.getId())
                .put("name", channel.getName())
                .put("description", channel.getDescription()));
        }
    }

    @CordovaMethod
    private void listChannels(CallbackContext callbackContext) throws JSONException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Notification channels are not supported");
        }

        List<NotificationChannel> channels = notificationManager.getNotificationChannels();
        JSONArray result = new JSONArray();
        for (NotificationChannel channel : channels) {
            result.put(new JSONObject()
                .put("id", channel.getId())
                .put("name", channel.getName())
                .put("description", channel.getDescription()));
        }

        callbackContext.success(result);
    }

    @CordovaMethod
    private void deleteChannel(String channelId, CallbackContext callbackContext) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Notification channels are not supported");
        }

        notificationManager.deleteNotificationChannel(channelId);

        callbackContext.success();
    }

    @Override
    public void onNewIntent(Intent intent) {
        JSONObject notificationData = getNotificationData(intent);
        if (instance != null && notificationData != null) {
            sendNotification(notificationData, instance.backgroundCallback);
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        this.isBackground = true;
    }

    @Override
    public void onResume(boolean multitasking) {
        this.isBackground = false;
    }

    static void sendNotification(RemoteMessage remoteMessage) {
        JSONObject notificationData = new JSONObject(remoteMessage.getData());
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        try {
            if (notification != null) {
                notificationData.put("gcm", toJSON(notification));
            }
            notificationData.put("google.message_id", remoteMessage.getMessageId());
            notificationData.put("google.sent_time", remoteMessage.getSentTime());

            if (instance != null) {
                CallbackContext callbackContext = instance.isBackground ?
                    instance.backgroundCallback : instance.foregroundCallback;
                instance.sendNotification(notificationData, callbackContext);
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendNotification", e);
        }
    }

    static void sendToken(String instanceId) {
        if (instance != null) {
            if (instance.tokenRefreshCallback != null && instanceId != null) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, instanceId);
                pluginResult.setKeepCallback(true);
                instance.tokenRefreshCallback.sendPluginResult(pluginResult);
            }
        }
    }

    static boolean isForceShow() {
        return instance != null && instance.forceShow;
    }

    private void sendNotification(JSONObject notificationData, CallbackContext callbackContext) {
        if (callbackContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, notificationData);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }
    }

    private JSONObject getNotificationData(Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle == null) {
            return null;
        }

        if (!bundle.containsKey("google.message_id") && !bundle.containsKey("google.sent_time")) {
            return null;
        }

        try {
            JSONObject notificationData = new JSONObject();
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                notificationData.put(key, bundle.get(key));
            }
            return notificationData;
        } catch (JSONException e) {
            Log.e(TAG, "getNotificationData", e);
            return null;
        }
    }

    private static JSONObject toJSON(RemoteMessage.Notification notification) throws JSONException {
        JSONObject result = new JSONObject()
            .put("body", notification.getBody())
            .put("title", notification.getTitle())
            .put("sound", notification.getSound())
            .put("icon", notification.getIcon())
            .put("tag", notification.getTag())
            .put("color", notification.getColor())
            .put("clickAction", notification.getClickAction());

        Uri imageUri = notification.getImageUrl();
        if (imageUri != null) {
            result.put("imageUrl", imageUri.toString());
        }

        return result;
    }
}
