#import "AppDelegate.h"

@import UserNotifications;
@import FirebaseMessaging;

@interface AppDelegate (FirebaseMessagingPlugin) <FIRMessagingDelegate, UNUserNotificationCenterDelegate>

@end
