#import "AppDelegate.h"

@interface AppDelegate (FirebaseMessagingPlugin)

- (void)postNotification:(NSDictionary*)userInfo background:(BOOL)value;

@end