#import "FirebaseMessagingPlugin.h"
#import <Cordova/CDV.h>
#import "AppDelegate.h"

@import FirebaseInstanceID;
@import FirebaseMessaging;
@import FirebaseAnalytics;

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
@import UserNotifications;
#endif

// Copied from Apple's header in case it is missing in some cases (e.g. pre-Xcode 8 builds).
#ifndef NSFoundationVersionNumber_iOS_9_x_Max
#define NSFoundationVersionNumber_iOS_9_x_Max 1299
#endif

@implementation FirebaseMessagingPlugin

- (void)requestPermission:(CDVInvokedUrlCommand *)command {
    self.registerCallbackId = command.callbackId;
    // Register for remote notifications. This shows a permission dialog on first run, to
    // show the dialog at a more appropriate time move this registration accordingly.
    if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_7_1) {
        // iOS 7.1 or earlier. Disable the deprecation warnings.
        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wdeprecated-declarations"
        UIRemoteNotificationType allNotificationTypes =
            (UIRemoteNotificationTypeSound |
             UIRemoteNotificationTypeAlert |
             UIRemoteNotificationTypeBadge);
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:allNotificationTypes];
        #pragma clang diagnostic pop
    } else {
        // iOS 8 or later
        // [START register_for_notifications]
        if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
          UIUserNotificationType allNotificationTypes =
          (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
          UIUserNotificationSettings *settings =
          [UIUserNotificationSettings settingsForTypes:allNotificationTypes categories:nil];
          [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        } else {
          // iOS 10 or later
          #if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
          // For iOS 10 display notification (sent via APNS)
          [UNUserNotificationCenter currentNotificationCenter].delegate = [FIRMessaging messaging].delegate;
          UNAuthorizationOptions authOptions =
              UNAuthorizationOptionAlert
              | UNAuthorizationOptionSound
              | UNAuthorizationOptionBadge;
          [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:authOptions completionHandler:^(BOOL granted, NSError * _Nullable error) {
              }];
          #endif
        }

        [[UIApplication sharedApplication] registerForRemoteNotifications];
        // [END register_for_notifications]
    }
}

- (void)getToken:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* currentToken = [[FIRInstanceID instanceID] token];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:currentToken];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)setBadge:(CDVInvokedUrlCommand *)command {
    int number = [[command.arguments objectAtIndex:0] intValue];

    [self.commandDelegate runInBackground:^{
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:number];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)getBadge:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        long badge = [[UIApplication sharedApplication] applicationIconBadgeNumber];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:badge];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)subscribe:(CDVInvokedUrlCommand *)command {
    NSString* topic = [NSString stringWithFormat:@"/topics/%@", [command.arguments objectAtIndex:0]];

    [[FIRMessaging messaging] subscribeToTopic: topic];

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)unsubscribe:(CDVInvokedUrlCommand *)command {
    NSString* topic = [NSString stringWithFormat:@"/topics/%@", [command.arguments objectAtIndex:0]];

    [[FIRMessaging messaging] unsubscribeFromTopic: topic];

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)onMessage:(CDVInvokedUrlCommand *)command {
    self.notificationCallbackId = command.callbackId;
}

- (void)onBackgroundMessage:(CDVInvokedUrlCommand *)command {
    self.backgroundNotificationCallbackId = command.callbackId;

    if (self.lastNotification) {
        [self sendBackgroundNotification:self.lastNotification];

        self.lastNotification = nil;
    }
}

- (void)onTokenRefresh:(CDVInvokedUrlCommand *)command {
    self.tokenRefreshCallbackId = command.callbackId;
}

- (void)registerNotifications:(NSError *)error {
    if (self.registerCallbackId) {
        CDVPluginResult *pluginResult;

        if (error) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        } else {
            NSData* deviceToken = [FIRMessaging messaging].APNSToken;

            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArrayBuffer:deviceToken];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.registerCallbackId];
    }
}

- (void)sendNotification:(NSDictionary *)userInfo {
    if (self.notificationCallbackId) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userInfo];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.notificationCallbackId];
    }
}

- (void)sendBackgroundNotification:(NSDictionary *)userInfo {
    if (self.backgroundNotificationCallbackId) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userInfo];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.backgroundNotificationCallbackId];
    } else {
        self.lastNotification = userInfo;
    }
}

- (void)refreshToken:(NSString *)token {
    if (self.tokenRefreshCallbackId != nil) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.tokenRefreshCallbackId];
    }
}

@end