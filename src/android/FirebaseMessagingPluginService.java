package by.chemerisuk.cordova.firebase;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;


public class FirebaseMessagingPluginService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingPluginService";

    public static final String ACTION_FCM_MESSAGE = "by.chemerisuk.cordova.firebase.ACTION_FCM_MESSAGE";
    public static final String EXTRA_FCM_MESSAGE = "by.chemerisuk.cordova.firebase.EXTRA_FCM_MESSAGE";
    public static final String ACTION_FCM_TOKEN = "by.chemerisuk.cordova.firebase.ACTION_FCM_TOKEN";
    public static final String EXTRA_FCM_TOKEN = "by.chemerisuk.cordova.firebase.EXTRA_FCM_TOKEN";
    public final static String NOTIFICATION_ICON_KEY = "com.google.firebase.messaging.default_notification_icon";
    public final static String NOTIFICATION_COLOR_KEY = "com.google.firebase.messaging.default_notification_color";

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private LocalBroadcastManager broadcastManager;
    private NotificationManager notificationManager;
    private int defaultNotificationIcon;
    private int defaultNotificationColor;

    @Override
    public void onCreate() {
        this.broadcastManager = LocalBroadcastManager.getInstance(this);
        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String sound = notification.getSound();
            if (sound != null && FirebaseMessagingPlugin.isForcePlaySound()) {
                if (!sound.equals("default") && !sound.equals("enabled")) {
                    soundUri = Uri.parse(SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/raw/" + sound);
                }
                if (!FirebaseMessagingPlugin.isForceShowAlert()) {
                    playSound(soundUri);
                }
            }

            if (FirebaseMessagingPlugin.isForceShowAlert()) {
                showAlert(notification, soundUri);
            }
        }
    }

    private void showAlert(RemoteMessage.Notification notification, Uri soundUri) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notification.getChannelId());
        builder.setContentTitle(notification.getTitle());
        builder.setContentText(notification.getBody());
        builder.setGroup(notification.getTag());
        builder.setSmallIcon(this.defaultNotificationIcon);
        builder.setColor(this.defaultNotificationColor);
        // always show alert, must set sound and priority
        builder.setPriority(1);
        builder.setSound(soundUri);

        this.notificationManager.notify(0, builder.build());
        // dismiss notification from status bar automatically
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(0);
            }
        }, 3000);
    }

    private void playSound(Uri soundUri) {
        if (this.audioManager.getMode() == AudioManager.RINGER_MODE_NORMAL) {
            if (this.mediaPlayer == null) {
                this.mediaPlayer = new MediaPlayer();
            }
            try {
                this.mediaPlayer.setDataSource(this, soundUri);
                this.mediaPlayer.prepare();
                this.mediaPlayer.start();
            } catch (IOException e) {
                Log.e(TAG, "Can't play sound in server response", e);
            }
        }
    }
}
