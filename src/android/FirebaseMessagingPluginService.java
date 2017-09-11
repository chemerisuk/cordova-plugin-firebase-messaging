package by.chemerisuk.cordova.firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;


public class FirebaseMessagingPluginService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingPlugin";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
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

            FirebaseMessagingPlugin.sendNotification(json);
        } catch (JSONException e) {
            Log.e(TAG, "onMessageReceived", e);
        }
    }
}
