# cordova-plugin-firebase-messaging<br>[![NPM version][npm-version]][npm-url] [![NPM downloads][npm-downloads]][npm-url]
> Cordova plugin for [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)

## Installation

    cordova plugin add cordova-plugin-firebase-messaging --save

## Supported Platforms

- iOS
- Android

## Methods

### onMessage(_callback_)
Called when a message is received.
```js
window.cordova.plugins.firebase.messaging.onMessage(function(payload) {
    console.log("New FCM message: ", payload);
});
```

__In general (for both platforms) you can only rely on custom data fields from a FCM payload__.

For iOS APNS payload is stored in `aps` object. It's available when a message arrives in both foreground and background.

For Android GCM payload is stored in `gcm`. It's available ONLY when a message arrives in foreground. For a some reason Google applied this limitation into their APIs. Anyway I've created [an issue](https://github.com/chemerisuk/cordova-plugin-firebase-messaging/issues/2) for a future improvement.

### onTokenRefresh(_callback_)
Logs an instance id token received.
```js
window.cordova.plugins.firebase.messaging.onTokenRefresh(function(token) {
    console.log("Got device token: ", token);
});
```

Use this callback to get initial token and to refresh stored value in future.

### subscribe(_topic_)
Subscribe to topic in background.
```js
window.cordova.plugins.firebase.messaging.subscribe("New Topic");
```

### unsubscribe(_topic_)
Unsubscribe from topic in background.
```js
window.cordova.plugins.firebase.messaging.unsubscribe("New Topic");
```

### getBadge(_callback_)
Reads current badge number (if supported).
```js
window.cordova.plugins.firebase.messaging.getBadge(function(value) {
    console.log("Badge value: ", value);
});
```

### setBadge(_value_)
Sets current badge number (if supported).
```js
window.cordova.plugins.firebase.messaging.setBadge(value);
```

### requestPermission (iOS only)
Grant permission to recieve push notifications (will trigger prompt).
```js
window.cordova.plugins.firebase.messaging.requestPermission();
```

[npm-url]: https://www.npmjs.com/package/cordova-plugin-firebase-messaging
[npm-version]: https://img.shields.io/npm/v/cordova-plugin-firebase-messaging.svg
[npm-downloads]: https://img.shields.io/npm/dt/cordova-plugin-firebase-messaging.svg
