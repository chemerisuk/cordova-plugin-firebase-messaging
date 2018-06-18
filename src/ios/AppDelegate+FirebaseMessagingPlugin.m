#import "AppDelegate+FirebaseMessagingPlugin.h"
#import "FirebaseMessagingPlugin.h"
#import <objc/runtime.h>

@implementation AppDelegate (FirebaseMessagingPlugin)

- (void)postNotification:(NSDictionary*)userInfo background:(BOOL)background {
    // Print full message.
    NSLog(@"%@", userInfo);

    NSDictionary *mutableUserInfo = [userInfo mutableCopy];
    // [mutableUserInfo setValue:userInfo[@"aps"] forKey:@"notification"];

    FirebaseMessagingPlugin* fmPlugin = [self.viewController getCommandInstance:@"FirebaseMessaging"];
    if (background) {
        [fmPlugin sendBackgroundNotification:mutableUserInfo];
    } else {
        [fmPlugin sendNotification:mutableUserInfo];
    }
}

// [START receive_message]
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    BOOL value = application.applicationState != UIApplicationStateActive;

    [self postNotification:userInfo background:value];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
    fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    BOOL value = application.applicationState != UIApplicationStateActive;

    [self postNotification:userInfo background:value];

    completionHandler(UIBackgroundFetchResultNewData);
}
// [END receive_message]

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    FirebaseMessagingPlugin* fmPlugin = [self.viewController getCommandInstance:@"FirebaseMessaging"];

    [fmPlugin registerNotifications:error];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    FirebaseMessagingPlugin* fmPlugin = [self.viewController getCommandInstance:@"FirebaseMessaging"];

    [fmPlugin registerNotifications:nil];
}

@end