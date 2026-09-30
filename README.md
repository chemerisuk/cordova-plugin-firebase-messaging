# Cordova plugin for [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)
[![NPM version][npm-version]][npm-url] [![NPM downloads][npm-downloads]][npm-url] [![NPM total downloads][npm-total-downloads]][npm-url] [![GitHub sponsor](https://img.shields.io/static/v1?label=sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/chemerisuk) [![PayPal donate](https://img.shields.io/badge/paypal-donate-ff69b4?logo=paypal)][donate-url] [![Twitter][twitter-follow]][twitter-url]

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

Use variables `IOS_FIREBASE_SDK_VERSION` and `ANDROID_FIREBASE_BOM_VERSION` to override SDK versions for different platforms:

    $ cordova plugin add cordova-plugin-firebase-messaging \
        --variable IOS_FIREBASE_SDK_VERSION="9.3.0" \
        --variable ANDROID_FIREBASE_BOM_VERSION="30.3.1"

**For cordova-ios below version 8**: if you get an error about CocoaPods being unable to find compatible versions, run

    $ pod repo update

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

<a id="pushpayload"></a>

### PushPayload

> **PushPayload** = `object`

In general (for both platforms) you can only rely on custom data fields.

`message_id` and `sent_time` have `google.` prefix in property name (__will be fixed__).

#### Properties

| Property | Type | Description |
| :------ | :------ | :------ |
| <a id="aps"></a> `aps?` | `Record`\<`string`, `any`\> | IOS payload, available when message arrives in both foreground and background. |
| <a id="data"></a> `data` | `Record`\<`string`, `any`\> | Custom data sent from server |
| <a id="gcm"></a> `gcm?` | `Record`\<`string`, `any`\> | Android payload, available ONLY when message arrives in foreground. |
| <a id="message_id"></a> `message_id` | `string` | Message ID automatically generated by the server |
| <a id="sent_time"></a> `sent_time` | `number` | Time in milliseconds from the Epoch that the message was sent. |

## Functions

<a id="clearnotifications"></a>

### clearNotifications()

> **clearNotifications**(): `Promise`\<`void`\>

Clear all notifications from system notification bar.

#### Returns

`Promise`\<`void`\>

Callback when operation is completed

#### Example

```ts
cordova.plugins.firebase.messaging.clearNotifications();
```

***

<a id="deletetoken"></a>

### deleteToken()

> **deleteToken**(): `Promise`\<`void`\>

Delete the Instance ID (Token) and the data associated with it.

Call getToken to generate a new one.

#### Returns

`Promise`\<`void`\>

Callback when operation is completed

#### Example

```ts
cordova.plugins.firebase.messaging.deleteToken();
```

***

<a id="getbadge"></a>

### getBadge()

> **getBadge**(): `Promise`\<`number`\>

Gets current badge number (if supported).

#### Returns

`Promise`\<`number`\>

Promise fulfiled with the current badge value

#### Example

```ts
cordova.plugins.firebase.messaging.getBadge().then(function(value) {
    console.log("Badge value: ", value);
});
```

***

<a id="gettoken"></a>

### getToken()

> **getToken**(`format?`: `"apns-buffer"` \| `"apns-string"`): `Promise`\<`string`\>

Returns the current FCM token.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `format?` | `"apns-buffer"` \| `"apns-string"` | Token representation (iOS only) |

#### Returns

`Promise`\<`string`\>

Promise fulfiled with the current FCM token

#### Example

```ts
cordova.plugins.firebase.messaging.getToken().then(function(token) {
    console.log("Got device token: ", token);
});
```

***

<a id="onbackgroundmessage"></a>

### onBackgroundMessage()

> **onBackgroundMessage**(`callback`: (`payload`: [`PushPayload`](#pushpayload)) => `void`, `errorCallback?`: (`error`: `string`) => `void`): `void`

Registers background push notification callback.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `callback` | (`payload`: [`PushPayload`](#pushpayload)) => `void` | Callback function |
| `errorCallback?` | (`error`: `string`) => `void` | Error callback function |

#### Returns

`void`

#### Example

```ts
cordova.plugins.firebase.messaging.onBackgroundMessage(function(payload) {
    console.log("New background FCM message: ", payload);
});
```

***

<a id="onmessage"></a>

### onMessage()

> **onMessage**(`callback`: (`payload`: [`PushPayload`](#pushpayload)) => `void`, `errorCallback?`: (`error`: `string`) => `void`): `void`

Registers foreground push notification callback.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `callback` | (`payload`: [`PushPayload`](#pushpayload)) => `void` | Callback function |
| `errorCallback?` | (`error`: `string`) => `void` | Error callback function |

#### Returns

`void`

#### Example

```ts
cordova.plugins.firebase.messaging.onMessage(function(payload) {
    console.log("New foreground FCM message: ", payload);
});
```

***

<a id="ontokenrefresh"></a>

### onTokenRefresh()

> **onTokenRefresh**(`callback`: () => `void`, `errorCallback?`: (`error`: `string`) => `void`): `void`

Registers callback to notify when FCM token is updated.

Use `getToken` to generate a new token.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `callback` | () => `void` | Callback function |
| `errorCallback?` | (`error`: `string`) => `void` | Error callback function |

#### Returns

`void`

#### Example

```ts
cordova.plugins.firebase.messaging.onTokenRefresh(function() {
    console.log("Device token updated");
});
```

***

<a id="requestpermission"></a>

### requestPermission()

> **requestPermission**(`options`: `object`): `Promise`\<`void`\>

Ask for permission to recieve push notifications (will trigger prompt on iOS).

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `options` | \{ `forceShow`: `boolean`; \} | Additional options. |
| `options.forceShow` | `boolean` | When value is `true` incoming notification is displayed even when app is in foreground. |

#### Returns

`Promise`\<`void`\>

Filfiled promise when permission is granted.

#### Example

```ts
cordova.plugins.firebase.messaging.requestPermission({forceShow: false}).then(function() {
    console.log("Push messaging is allowed");
});
```

***

<a id="setbadge"></a>

### setBadge()

> **setBadge**(`badgeValue`: `number`): `Promise`\<`void`\>

Sets current badge number (if supported).

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `badgeValue` | `number` | New badge value |

#### Returns

`Promise`\<`void`\>

Callback when operation is completed

#### Example

```ts
cordova.plugins.firebase.messaging.setBadge(value);
```

***

<a id="streambackgroundmessage"></a>

### streamBackgroundMessage()

> **streamBackgroundMessage**(`signal?`: `AbortSignal`): `AsyncGenerator`\<[`PushPayload`](#pushpayload), `void`, `unknown`\>

Subscribes to the stream of incoming push notifications received while the app is in the background.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `signal?` | `AbortSignal` | An optional signal to abort the stream. |

#### Returns

`AsyncGenerator`\<[`PushPayload`](#pushpayload), `void`, `unknown`\>

An async generator yielding message payloads.

#### Example

```ts
const controller = new AbortController();
for await (const message of FirebaseMessaging.streamBackgroundMessage(controller.signal)) {
    console.log("Received a background push notification:", message);
}
```

***

<a id="streammessage"></a>

### streamMessage()

> **streamMessage**(`signal?`: `AbortSignal`): `AsyncGenerator`\<[`PushPayload`](#pushpayload), `void`, `unknown`\>

Subscribes to the stream of incoming push notifications received while the app is in the foreground.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `signal?` | `AbortSignal` | An optional signal to abort the stream. |

#### Returns

`AsyncGenerator`\<[`PushPayload`](#pushpayload), `void`, `unknown`\>

An async generator yielding message payloads.

#### Example

```ts
const controller = new AbortController();
for await (const message of FirebaseMessaging.streamMessage(controller.signal)) {
    console.log("Received a foreground push notification:", message);
}
```

***

<a id="streamtokenrefresh"></a>

### streamTokenRefresh()

> **streamTokenRefresh**(`signal?`: `AbortSignal`): `AsyncGenerator`\<`string`, `void`, `unknown`\>

Subscribes to the FCM token refresh event stream.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `signal?` | `AbortSignal` | An optional signal to abort the stream. |

#### Returns

`AsyncGenerator`\<`string`, `void`, `unknown`\>

An async generator yielding refreshed token strings.

#### Example

```ts
const controller = new AbortController();
for await (const token of FirebaseMessaging.streamTokenRefresh(controller.signal)) {
    console.log("New FCM Token arrived:", token);
}
```

***

<a id="subscribe"></a>

### subscribe()

> **subscribe**(`topic`: `string`): `Promise`\<`void`\>

Subscribe to a FCM topic.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `topic` | `string` | Topic name |

#### Returns

`Promise`\<`void`\>

Callback when operation is completed

#### Example

```ts
cordova.plugins.firebase.messaging.subscribe("news");
```

***

<a id="unsubscribe"></a>

### unsubscribe()

> **unsubscribe**(`topic`: `string`): `Promise`\<`void`\>

Unsubscribe from a FCM topic.

#### Parameters

| Parameter | Type | Description |
| :------ | :------ | :------ |
| `topic` | `string` | Topic name |

#### Returns

`Promise`\<`void`\>

Callback when operation is completed

#### Example

```ts
cordova.plugins.firebase.messaging.unsubscribe("news");
```
