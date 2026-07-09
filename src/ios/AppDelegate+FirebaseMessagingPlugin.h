#import <Cordova/CDVAppDelegate.h>

@import UserNotifications;
@import FirebaseMessaging;

@interface CDVAppDelegate (FirebaseMessagingPlugin) <FIRMessagingDelegate, UNUserNotificationCenterDelegate>

@end
