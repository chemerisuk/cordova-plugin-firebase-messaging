#import "AppDelegate.h"

@import Firebase;

@interface AppDelegate (FirebaseMessagingPlugin) <FIRMessagingDelegate>

- (void)postNotification:(NSDictionary*) userInfo background:(BOOL) value;

@end