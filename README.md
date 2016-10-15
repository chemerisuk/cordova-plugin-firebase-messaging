# cordova-plugin-firebase-messaging
> Cordova plugin for [Firebase Messaging](https://firebase.google.com/docs/cloud-messaging/)

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

### onTokenRefresh(_callback_)
Logs an instance id token received.
```js
window.cordova.plugins.firebase.messaging.onTokenRefresh(function(token) {
    console.log("Got device token: ", token);
});
```

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
