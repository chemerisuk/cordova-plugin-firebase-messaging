# cordova-plugin-firebase-messaging<br>[![NPM version][npm-version]][npm-url] [![NPM downloads][npm-downloads]][npm-url]
> Cordova plugin for [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)

## Installation

    cordova plugin add cordova-plugin-firebase-messaging --save

Use variables `FIREBASE_MESSAGING_VERSION` and `ANDROID_SUPPORT_VERSION` to override dependency versions on Android.

Plugin depends on [cordova-support-google-services](https://github.com/chemerisuk/cordova-support-google-services) for setting up google services properly. Please read the [README](https://github.com/chemerisuk/cordova-support-google-services/blob/master/README.md) carefully in order to avoid common issues with a project configuration.

## Supported Platforms

- iOS
- Android

## Methods
In general (for both platforms) you can only rely on custom data fields from a FCM payload.

For iOS APNS payload is stored in `aps` object. It's available when a message arrives in both foreground and background.

For Android GCM payload is stored in `gcm`. It's available ONLY when a message arrives in foreground. For a some reason Google applied this limitation into their APIs. Anyway I've created [an issue](https://github.com/chemerisuk/cordova-plugin-firebase-messaging/issues/2) for a future improvement.

### onMessage(_callback_)
Called when a push message received while app is in foreground.
```js
cordova.plugins.firebase.messaging.onMessage(function(payload) {
    console.log("New foreground FCM message: ", payload);
});
```

### onBackgroundMessage(_callback_)
Called when a push message received while app is in background.
```js
cordova.plugins.firebase.messaging.onBackgroundMessage(function(payload) {
    console.log("New background FCM message: ", payload);
});
```

### requestPermission
Grant permission to recieve push notifications (will trigger prompt on iOS).
```js
cordova.plugins.firebase.messaging.requestPermission().then(function(token) {
    console.log("APNS device token: ", token);
});
```

### getToken
Returns a promise that fulfills with the current FCM token
```js
cordova.plugins.firebase.messaging.getToken().then(function(token) {
    console.log("Got device token: ", token);
});
```

### onTokenRefresh(_callback_)
Triggers every time when FCM token updated. You should usually call `getToken` to get an updated token and send it to server.
```js
cordova.plugins.firebase.messaging.onTokenRefresh(function() {
    console.log("Device token updated");
});
```

Use this callback to get initial token and to refresh stored value in future.

### subscribe(_topic_)
Subscribe to topic in background.
```js
cordova.plugins.firebase.messaging.subscribe("New Topic");
```

### unsubscribe(_topic_)
Unsubscribe from topic in background.
```js
cordova.plugins.firebase.messaging.unsubscribe("New Topic");
```

### getBadge(_callback_)
Reads current badge number (if supported).
```js
cordova.plugins.firebase.messaging.getBadge().then(function(value) {
    console.log("Badge value: ", value);
});
```

### setBadge(_value_)
Sets current badge number (if supported).
```js
cordova.plugins.firebase.messaging.setBadge(value);
```

## Set custom default notification channel (Android only)
If incoming FCM messages do not specify an Android notification channel, you can indicate to FCM what channel should be used as the default by adding a metadata element to your application manifest. In the metadata element specify the ID of the channel that should be used by default by FCM.
```
<config-file parent="/manifest/application" target="app/src/main/AndroidManifest.xml">
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_channel_id"
        android:value="default_channel_id"/>
</config-file>
```

Note: You are still required to create a notification channel in code with an ID that matches the one defined in the manifest. See the [Android docs](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) for more.

## Set custom default notification icon (Android only)
Setting a custom default icon allows you to specify what icon is used for notification messages if no icon is set in the notification payload. Also use the custom default icon to set the icon used by notification messages sent from the Firebase console. If no custom default icon is set and no icon is set in the notification payload, the application icon (rendered in white) is used.
```
<config-file parent="/manifest/application" target="app/src/main/AndroidManifest.xml">
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_icon"
        android:resource="@drawable/my_custom_icon_id"/>
</config-file>
```

## Set custom default notification color (Android only)
You can also define what color is used with your notification. Different android versions use this settings in different ways: Android < N use this as background color for the icon. Android >= N use this to color the icon and the app name.
```
<config-file parent="/manifest/application" target="app/src/main/AndroidManifest.xml">
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_color"
        android:resource="@drawable/my_custom_color"/>
</config-file>
```

[npm-url]: https://www.npmjs.com/package/cordova-plugin-firebase-messaging
[npm-version]: https://img.shields.io/npm/v/cordova-plugin-firebase-messaging.svg
[npm-downloads]: https://img.shields.io/npm/dm/cordova-plugin-firebase-messaging.svg
