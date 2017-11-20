# cordova-plugin-firebase-messaging<br>[![NPM version][npm-version]][npm-url] [![NPM downloads][npm-downloads]][npm-url]
> Cordova plugin for [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)

## Installation

    cordova plugin add cordova-plugin-firebase-messaging --save

If you need to set a specific dependency version on Android then use variable `FIREBASE_VERSION`.

Plugin depends on [cordova-support-google-services](https://github.com/chemerisuk/cordova-support-google-services) for setting up google services properly. Please read the [README](https://github.com/chemerisuk/cordova-support-google-services/blob/master/README.md) carefully in order to avoid common issues with a project configuration.

## Supported Platforms

- iOS
- Android

## Methods
In general (for both platforms) you can only rely on custom data fields from a FCM payload.

For iOS APNS payload is stored in `aps` object. It's available when a message arrives in both foreground and background.

For Android GCM payload is stored in `gcm`. It's available ONLY when a message arrives in foreground. For a some reason Google applied this limitation into their APIs. Anyway I've created [an issue](https://github.com/chemerisuk/cordova-plugin-firebase-messaging/issues/2) for a future improvement.

### onMessage(_callback_)
Called when a push message received in FOREGROUND.
```js
cordova.plugins.firebase.messaging.onMessage(function(payload) {
    console.log("New foreground FCM message: ", payload);
});
```

### onBackgroundMessage(_callback_)
Called when a push message received in FOREGROUND.
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

[npm-url]: https://www.npmjs.com/package/cordova-plugin-firebase-messaging
[npm-version]: https://img.shields.io/npm/v/cordova-plugin-firebase-messaging.svg
[npm-downloads]: https://img.shields.io/npm/dm/cordova-plugin-firebase-messaging.svg
