package by.chemerisuk.cordova.firebase;

import java.util.HashMap;

import android.content.Intent;
import android.util.Log;
// import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessagingPluginService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingPlugin";
    // public static final String ACTION_FCM_DATA = "by.chemerisuk.cordova.firebase.FCM_DATA_EVENT";
    // public static final String KEY_FCM_DATA = "RemoteMessageData";

    // private LocalBroadcastManager broadcastManager;

    // @Override
    // public void onCreate() {
    //     this.broadcastManager = LocalBroadcastManager.getInstance(this);
    // }

    @Override
    public void onNewToken(String token) {
        FirebaseMessagingPlugin.sendInstanceId(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        FirebaseMessagingPlugin.sendNotification(remoteMessage);

        // if (!remoteMessage.getData().isEmpty()) {
        //     Intent intent = new Intent(ACTION_FCM_DATA);
        //     intent.putExtra(KEY_FCM_DATA, new HashMap(remoteMessage.getData()));
        //     this.broadcastManager.sendBroadcast(intent);
        // }
    }
}
