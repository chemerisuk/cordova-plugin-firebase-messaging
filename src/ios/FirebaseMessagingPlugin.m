#import "FirebaseMessagingPlugin.h"
#import <Cordova/CDV.h>
#import "AppDelegate.h"

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
@import UserNotifications;

// Implement UNUserNotificationCenterDelegate to receive display notification via APNS for devices
// running iOS 10 and above. Implement FIRMessagingDelegate to receive data message via FCM for
// devices running iOS 10 and above.
@interface FirebaseMessagingPlugin () <FIRMessagingDelegate, UNUserNotificationCenterDelegate>
@end
#endif

// Copied from Apple's header in case it is missing in some cases (e.g. pre-Xcode 8 builds).
#ifndef NSFoundationVersionNumber_iOS_9_x_Max
#define NSFoundationVersionNumber_iOS_9_x_Max 1299
#endif

@implementation FirebaseMessagingPlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Messaging plugin");

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(finishLaunching:) name:UIApplicationDidFinishLaunchingNotification object:nil];

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
}

- (void)finishLaunching:(NSNotification *)notification {
    [FIRMessaging messaging].delegate = self;
    // iOS 10 or later
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
    [UNUserNotificationCenter currentNotificationCenter].delegate = self;
#endif

    if (notification) {
        NSDictionary *launchOptions = [notification userInfo];
        if (launchOptions) {
            NSDictionary *userInfo = launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey];
            if (userInfo) {
                [self sendBackgroundNotification:userInfo];
            }
        }
    }
}

- (void)requestPermission:(CDVInvokedUrlCommand *)command {
    self.registerCallbackId = command.callbackId;

    if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
        UIUserNotificationType allNotificationTypes =
        (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationActivationModeBackground);
        UIUserNotificationSettings *settings =
        [UIUserNotificationSettings settingsForTypes:allNotificationTypes categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    } else {
        // iOS 10 or later
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
        UNAuthorizationOptions authOptions =
        (UNAuthorizationOptionAlert | UNAuthorizationOptionSound | UNAuthorizationOptionBadge);
        [[UNUserNotificationCenter currentNotificationCenter]
         requestAuthorizationWithOptions:authOptions
         completionHandler:^(BOOL granted, NSError * _Nullable error) {
             // if (error) {
             //     [self registerNotifications:error];
             // } else if (granted) {
             //     [[UIApplication sharedApplication] registerForRemoteNotifications];
             // } else {
             //     // TODO?
             // }
         }];
#endif
    }

    [[UIApplication sharedApplication] registerForRemoteNotifications];
}

- (void)getToken:(CDVInvokedUrlCommand *)command {
    NSString *fcmToken = [FIRMessaging messaging].FCMToken;
    if (fcmToken) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:fcmToken];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        [[FIRInstanceID instanceID] instanceIDWithHandler:^(FIRInstanceIDResult * _Nullable result,
                                                            NSError * _Nullable error) {
            CDVPluginResult *pluginResult;
            if (error) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:result.token];
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    }
}

- (void)setBadge:(CDVInvokedUrlCommand *)command {
    int badge = [[command.arguments objectAtIndex:0] intValue];

    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badge];

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getBadge:(CDVInvokedUrlCommand *)command {
    int badge = (int)[[UIApplication sharedApplication] applicationIconBadgeNumber];

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:badge];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)subscribe:(CDVInvokedUrlCommand *)command {
    NSString* topic = [NSString stringWithFormat:@"/topics/%@", [command.arguments objectAtIndex:0]];

    [[FIRMessaging messaging] subscribeToTopic:topic
                                    completion:^(NSError * _Nullable error) {
                                        CDVPluginResult *pluginResult;
                                        if (error != nil) {
                                            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
                                        } else {
                                            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                                        }
                                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                                    }];
}

- (void)unsubscribe:(CDVInvokedUrlCommand *)command {
    NSString* topic = [NSString stringWithFormat:@"/topics/%@", [command.arguments objectAtIndex:0]];

    [[FIRMessaging messaging] unsubscribeFromTopic:topic
                                        completion:^(NSError * _Nullable error) {
                                            CDVPluginResult *pluginResult;
                                            if (error != nil) {
                                                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
                                            } else {
                                                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                                            }
                                            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                                        }];
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

# pragma mark - UNUserNotificationCenterDelegate
// Receive displayed notifications for iOS 10 devices.
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
// Handle incoming notification messages while app is in the foreground.
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    NSDictionary *userInfo = notification.request.content.userInfo;
    UNNotificationPresentationOptions options = UNNotificationPresentationOptionNone;

    [self sendNotification:userInfo];
    // Change this to your preferred presentation option
    completionHandler(options);
}

// Handle notification messages after display notification is tapped by the user.
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    NSDictionary *userInfo = response.notification.request.content.userInfo;
    [self sendBackgroundNotification:userInfo];

    completionHandler();
}
#endif

# pragma mark - FIRMessagingDelegate

- (void)messaging:(FIRMessaging *)messaging didReceiveRegistrationToken:(NSString *)fcmToken {
    if (self.tokenRefreshCallbackId != nil) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.tokenRefreshCallbackId];
    }
}

@end
