package by.chemerisuk.cordova.firebase;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ExecutionThread;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;
import me.leolin.shortcutbadger.ShortcutBadger;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.android.gms.tasks.Tasks.await;
import static by.chemerisuk.cordova.support.ExecutionThread.WORKER;

//--- begin: notification channel imports (GLS)
import android.app.NotificationChannel;
import android.media.AudioAttributes;
import android.content.ContentResolver;
import org.json.JSONArray;
import java.util.List;
import android.os.Build;
//---- end: notification channel imports

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

    @CordovaMethod(WORKER)
    private void subscribe(CordovaArgs args, final CallbackContext callbackContext) throws Exception {
        String topic = args.getString(0);
        await(firebaseMessaging.subscribeToTopic(topic));
        callbackContext.success();
    }

    @CordovaMethod(WORKER)
    private void unsubscribe(CordovaArgs args, CallbackContext callbackContext) throws Exception {
        String topic = args.getString(0);
        await(firebaseMessaging.unsubscribeFromTopic(topic));
        callbackContext.success();
    }

    @CordovaMethod
    private void clearNotifications(CallbackContext callbackContext) {
        notificationManager.cancelAll();
        callbackContext.success();
    }

    @CordovaMethod(WORKER)
    private void deleteToken(CallbackContext callbackContext) throws Exception {
        await(firebaseMessaging.deleteToken());
        callbackContext.success();
    }

    @CordovaMethod(WORKER)
    private void getToken(CordovaArgs args, CallbackContext callbackContext) throws Exception {
        String type = args.getString(0);
        if (!type.isEmpty()) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, (String)null));
        } else {
            String fcmToken = await(firebaseMessaging.getToken());
            callbackContext.success(fcmToken);
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
    private void setBadge(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        int value = args.getInt(0);
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
    private void requestPermission(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        Context context = cordova.getActivity().getApplicationContext();
        forceShow = options.optBoolean("forceShow");
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            callbackContext.success();
        } else {
            callbackContext.error("Notifications permission is not granted");
        }
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

    //----------------------------------------------------------------------------------
    //---- begin: notification channel implementation (GLS)
    //----------------------------------------------------------------------------------
    private static JSONObject toJSON(NotificationChannel channel) throws JSONException {
        JSONObject result = new JSONObject()
                .put("id", channel.getId())
                .put("name", channel.getName())
                .put("description", channel.getDescription())
                .put("importance", channel.getImportance())
                .put("badge", channel.canShowBadge())
                .put("light", channel.shouldShowLights())
                .put("lightColor", channel.getLightColor())
                .put("sound", channel.getSound())
                .put("vibration", channel.getVibrationPattern());

        return result;
    }

    @CordovaMethod
    private void createChannel(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Notification channels are not supported");
        }
        JSONObject options = args.getJSONObject(0);

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
    private void findChannel(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String channelId = args.getString(0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Notification channels are not supported");
        }

        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
        if (channel == null) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, (String)null));
        } else {
            callbackContext.success(toJSON(channel));
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
            result.put(toJSON(channel));
        }

        callbackContext.success(result);
    }

    @CordovaMethod
    private void deleteChannel(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String channelId = args.getString(0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Notification channels are not supported");
        }

        notificationManager.deleteNotificationChannel(channelId);

        callbackContext.success();
    }
    //----------------------------------------------------------------------------------
    //---- end: notification channel implementation (GLS)
    //----------------------------------------------------------------------------------
}
