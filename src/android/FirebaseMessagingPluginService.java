package by.chemerisuk.cordova.firebase;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;


public class FirebaseMessagingPluginService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingPluginService";

    public static final String ACTION_FCM_MESSAGE = "by.chemerisuk.cordova.firebase.ACTION_FCM_MESSAGE";
    public static final String EXTRA_FCM_MESSAGE = "by.chemerisuk.cordova.firebase.EXTRA_FCM_MESSAGE";
    public static final String ACTION_FCM_TOKEN = "by.chemerisuk.cordova.firebase.ACTION_FCM_TOKEN";
    public static final String EXTRA_FCM_TOKEN = "by.chemerisuk.cordova.firebase.EXTRA_FCM_TOKEN";
    public final static String NOTIFICATION_ICON_KEY = "com.google.firebase.messaging.default_notification_icon";
    public final static String NOTIFICATION_COLOR_KEY = "com.google.firebase.messaging.default_notification_color";

    private LocalBroadcastManager broadcastManager;
    private NotificationManager notificationManager;
    private int defaultNotificationIcon;
    private int defaultNotificationColor;

    @Override
    public void onCreate() {
        this.broadcastManager = LocalBroadcastManager.getInstance(this);
        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            this.defaultNotificationIcon = ai.metaData.getInt(NOTIFICATION_ICON_KEY, ai.icon);
            this.defaultNotificationColor = ContextCompat.getColor(this, ai.metaData.getInt(NOTIFICATION_COLOR_KEY));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data", e);
        } catch(Resources.NotFoundException e) {
            Log.e(TAG, "Failed to load notification color", e);
        }
    }

    @Override
    public void onNewToken(String token) {
        FirebaseMessagingPlugin.sendInstanceId(token);

        Intent intent = new Intent(ACTION_FCM_TOKEN);
        intent.putExtra(EXTRA_FCM_TOKEN, token);
        this.broadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        FirebaseMessagingPlugin.sendNotification(remoteMessage);

        Intent intent = new Intent(ACTION_FCM_MESSAGE);
        intent.putExtra(EXTRA_FCM_MESSAGE, remoteMessage);
        this.broadcastManager.sendBroadcast(intent);

        if (FirebaseMessagingPlugin.isForceShow()) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            if (notification != null) {
                showAlert(notification);
            }
        }
    }

    private void showAlert(RemoteMessage.Notification notification) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notification.getChannelId());
        builder.setContentTitle(notification.getTitle());
        builder.setContentText(notification.getBody());
        builder.setGroup(notification.getTag());
        builder.setSmallIcon(this.defaultNotificationIcon);
        builder.setColor(this.defaultNotificationColor);
        // must set sound and priority in order to display alert
        builder.setSound(getNotificationSound(notification.getSound()));
        builder.setPriority(1);

        this.notificationManager.notify(0, builder.build());
        // dismiss notification to hide icon from status bar automatically
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(0);
            }
        }, 3000);
    }

    private Uri getNotificationSound(String soundName) {
        if (soundName != null && !soundName.equals("default") && !soundName.equals("enabled")) {
            return Uri.parse(SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/raw/" + soundName);
        } else {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }
}
