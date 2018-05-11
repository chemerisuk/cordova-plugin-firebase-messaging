package by.chemerisuk.cordova.firebase;

import java.util.HashMap;

import android.content.Intent;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;


public class FirebaseMessagingPluginService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingPlugin";
    public static final String ACTION_FCM_DATA = "by.chemerisuk.cordova.firebase.FCM_DATA_EVENT";
    public static final String KEY_FCM_DATA = "RemoteMessageData";

    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        this.broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            JSONObject json = createNotificationJSON(remoteMessage);
            FirebaseMessagingPlugin.sendNotification(json, false);

            if (!remoteMessage.getData().isEmpty()) {
                Intent intent = new Intent(ACTION_FCM_DATA);
                intent.putExtra(KEY_FCM_DATA, new HashMap(remoteMessage.getData()));
                this.broadcastManager.sendBroadcast(intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, "onMessageReceived", e);
        }
    }

    private static JSONObject createNotificationJSON(RemoteMessage remoteMessage) throws JSONException {
        JSONObject json = new JSONObject(remoteMessage.getData());
        RemoteMessage.Notification notification = remoteMessage.getNotification();
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

        return json;
    }
}
