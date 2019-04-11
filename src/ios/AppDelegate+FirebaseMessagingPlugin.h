#import "AppDelegate.h"

@import UserNotifications;
@import Firebase;

@interface AppDelegate (FirebaseMessagingPlugin) <FIRMessagingDelegate, UNUserNotificationCenterDelegate>

- (void)postNotification:(NSDictionary*)userInfo background:(BOOL)value;

@end