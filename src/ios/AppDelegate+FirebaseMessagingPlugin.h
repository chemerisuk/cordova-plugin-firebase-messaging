#import "AppDelegate.h"

@import FirebaseMessaging;

@interface AppDelegate (FirebaseMessagingPlugin) <FIRMessagingDelegate>

- (void)postNotification:(NSDictionary*)userInfo background:(BOOL)value fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler;

@end