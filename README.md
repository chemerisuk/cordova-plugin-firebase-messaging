# Cordova plugin for [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)
[![NPM version][npm-version]][npm-url] [![NPM downloads][npm-downloads]][npm-url] [![NPM total downloads][npm-total-downloads]][npm-url] [![PayPal donate](https://img.shields.io/badge/paypal-donate-ff69b4?logo=paypal)][donate-url] [![Twitter][twitter-follow]][twitter-url]

| [![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)][donate-url] | Your help is appreciated. Create a PR, submit a bug or just grab me :beer: |
|-|-|

[npm-url]: https://www.npmjs.com/package/cordova-plugin-firebase-messaging
[npm-version]: https://img.shields.io/npm/v/cordova-plugin-firebase-messaging.svg
[npm-downloads]: https://img.shields.io/npm/dm/cordova-plugin-firebase-messaging.svg
[npm-total-downloads]: https://img.shields.io/npm/dt/cordova-plugin-firebase-messaging.svg?label=total+downloads
[twitter-url]: https://twitter.com/chemerisuk
[twitter-follow]: https://img.shields.io/twitter/follow/chemerisuk.svg?style=social&label=Follow%20me
[donate-url]: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6HLVTJDGQQ6EY&source=url

## Index

<!-- MarkdownTOC levels="2,3" autolink="true" -->

- [Supported platforms](#supported-platforms)
- [Installation](#installation)
- [Adding configuration files](#adding-configuration-files)
    - [Set custom default notification icon \(Android only\)](#set-custom-default-notification-icon-android-only)
    - [Set custom default notification color \(Android only\)](#set-custom-default-notification-color-android-only)
- [Type Aliases](#type-aliases)
    - [PushPayload](#pushpayload)
- [Functions](#functions)
    - [clearNotifications](#clearnotifications)
    - [deleteToken](#deletetoken)
    - [getBadge](#getbadge)
    - [getToken](#gettoken)
    - [onBackgroundMessage](#onbackgroundmessage)
    - [onMessage](#onmessage)
    - [onTokenRefresh](#ontokenrefresh)
    - [requestPermission](#requestpermission)
    - [setBadge](#setbadge)
    - [subscribe](#subscribe)
    - [unsubscribe](#unsubscribe)

<!-- /MarkdownTOC -->

## Supported platforms

- iOS
- Android

## Installation

    $ cordova plugin add cordova-plugin-firebase-messaging

If you get an error about CocoaPods being unable to find compatible versions, run
    
    $ pod repo update

Use variables `IOS_FIREBASE_POD_VERSION` and `ANDROID_FIREBASE_BOM_VERSION` to override dependency versions on Android:

    $ cordova plugin add cordova-plugin-firebase-messaging \
        --variable IOS_FIREBASE_POD_VERSION="9.3.0" \
        --variable ANDROID_FIREBASE_BOM_VERSION="30.3.1"

## Adding configuration files

Cordova supports `resource-file` tag for easy copying resources files. Firebase SDK requires `google-services.json` on Android and `GoogleService-Info.plist` on iOS platforms.

1. Put `google-services.json` and/or `GoogleService-Info.plist` into the root directory of your Cordova project
2. Add new tag for Android platform

```xml
<platform name="android">
    ...
    <resource-file src="google-services.json" target="app/google-services.json" />
</platform>
...
<platform name="ios">
    ...
    <resource-file src="GoogleService-Info.plist" />
</platform>
```

This way config files will be copied on `cordova prepare` step.

### Set custom default notification icon (Android only)
Setting a custom default icon allows you to specify what icon is used for notification messages if no icon is set in the notification payload. Also use the custom default icon to set the icon used by notification messages sent from the Firebase console. If no custom default icon is set and no icon is set in the notification payload, the application icon (rendered in white) is used.
```xml
<platform name="android">
    ...
    <config-file parent="/manifest/application" target="app/src/main/AndroidManifest.xml">
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_icon"
            android:resource="@drawable/my_custom_icon_id"/>
    </config-file>
</platform>
```

### Set custom default notification color (Android only)
You can also define what color is used with your notification. Different android versions use this settings in different ways: Android < N use this as background color for the icon. Android >= N use this to color the icon and the app name.
```xml
<platform name="android">
    ...
    <config-file parent="/manifest/application" target="app/src/main/AndroidManifest.xml">
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_color"
            android:resource="@drawable/my_custom_color"/>
    </config-file>
</platform>
```

<!-- TypedocGenerated -->

## Type Aliases

### PushPayload

 **PushPayload**: `Object`

In general (for both platforms) you can only rely on custom data fields.

`message_id` and `sent_time` have `google.` prefix in property name (__will be fixed__).

#### Type declaration

| Name | Type | Description |
| :------ | :------ | :------ |
| `aps?` | `Record`<`string`, `any`\> | IOS payload, available when message arrives in both foreground and background. |
| `data` | `Record`<`string`, `any`\> | Custom data sent from server |
| `gcm?` | `Record`<`string`, `any`\> | Android payload, available ONLY when message arrives in foreground. |
| `message_id` | `string` | Message ID automatically generated by the server |
| `sent_time` | `number` | Time in milliseconds from the Epoch that the message was sent. |

## Functions

### clearNotifications

**clearNotifications**(): `Promise`<`void`\>

Clear all notifications from system notification bar.

**`Example`**

```ts
cordova.plugins.firebase.messaging.clearNotifications();
```

#### Returns

`Promise`<`void`\>

Callback when operation is completed

___

### deleteToken

**deleteToken**(): `Promise`<`void`\>

Delete the Instance ID (Token) and the data associated with it.

Call getToken to generate a new one.

**`Example`**

```ts
cordova.plugins.firebase.messaging.deleteToken();
```

#### Returns

`Promise`<`void`\>

Callback when operation is completed

___

### getBadge

**getBadge**(): `Promise`<`number`\>

Gets current badge number (if supported).

**`Example`**

```ts
cordova.plugins.firebase.messaging.getBadge().then(function(value) {
    console.log("Badge value: ", value);
});
```

#### Returns

`Promise`<`number`\>

Promise fulfiled with the current badge value

___

### getToken

**getToken**(`format?`): `Promise`<`string`\>

Returns the current FCM token.

**`Example`**

```ts
cordova.plugins.firebase.messaging.getToken().then(function(token) {
    console.log("Got device token: ", token);
});
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `format?` | ``"apns-buffer"`` \| ``"apns-string"`` | Token representation (iOS only) |

#### Returns

`Promise`<`string`\>

Promise fulfiled with the current FCM token

___

### onBackgroundMessage

**onBackgroundMessage**(`callback`, `errorCallback?`): `void`

Registers background push notification callback.

**`Example`**

```ts
cordova.plugins.firebase.messaging.onBackgroundMessage(function(payload) {
    console.log("New background FCM message: ", payload);
});
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `callback` | (`payload`: [`PushPayload`](#pushpayload)) => `void` | Callback function |
| `errorCallback?` | (`error`: `string`) => `void` | Error callback function |

#### Returns

`void`

___

### onMessage

**onMessage**(`callback`, `errorCallback?`): `void`

Registers foreground push notification callback.

**`Example`**

```ts
cordova.plugins.firebase.messaging.onMessage(function(payload) {
    console.log("New foreground FCM message: ", payload);
});
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `callback` | (`payload`: [`PushPayload`](#pushpayload)) => `void` | Callback function |
| `errorCallback?` | (`error`: `string`) => `void` | Error callback function |

#### Returns

`void`

___

### onTokenRefresh

**onTokenRefresh**(`callback`, `errorCallback?`): `void`

Registers callback to notify when FCM token is updated.

Use `getToken` to generate a new token.

**`Example`**

```ts
cordova.plugins.firebase.messaging.onTokenRefresh(function() {
    console.log("Device token updated");
});
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `callback` | () => `void` | Callback function |
| `errorCallback?` | (`error`: `string`) => `void` | Error callback function |

#### Returns

`void`

___

### requestPermission

**requestPermission**(`options`): `Promise`<`void`\>

Ask for permission to recieve push notifications (will trigger prompt on iOS).

**`Example`**

```ts
cordova.plugins.firebase.messaging.requestPermission({forceShow: false}).then(function() {
    console.log("Push messaging is allowed");
});
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `options` | `Object` | Additional options. |
| `options.forceShow` | `boolean` | When value is `true` incoming notification is displayed even when app is in foreground. |

#### Returns

`Promise`<`void`\>

Filfiled promise when permission is granted.

___

### setBadge

**setBadge**(`badgeValue`): `Promise`<`void`\>

Sets current badge number (if supported).

**`Example`**

```ts
cordova.plugins.firebase.messaging.setBadge(value);
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `badgeValue` | `number` | New badge value |

#### Returns

`Promise`<`void`\>

Callback when operation is completed

___

### subscribe

**subscribe**(`topic`): `Promise`<`void`\>

Subscribe to a FCM topic.

**`Example`**

```ts
cordova.plugins.firebase.messaging.subscribe("news");
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `topic` | `string` | Topic name |

#### Returns

`Promise`<`void`\>

Callback when operation is completed

___

### unsubscribe

**unsubscribe**(`topic`): `Promise`<`void`\>

Unsubscribe from a FCM topic.

**`Example`**

```ts
cordova.plugins.firebase.messaging.unsubscribe("news");
```

#### Parameters

| Name | Type | Description |
| :------ | :------ | :------ |
| `topic` | `string` | Topic name |

#### Returns

`Promise`<`void`\>

Callback when operation is completed
