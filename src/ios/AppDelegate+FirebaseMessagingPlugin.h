#import "AppDelegate.h"

@import UserNotifications;
@import Firebase;

@interface AppDelegate (FirebaseMessagingPlugin) <FIRMessagingDelegate, UNUserNotificationCenterDelegate>

@end
