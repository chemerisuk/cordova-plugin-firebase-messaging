#import "AppDelegate+FirebaseMessagingPlugin.h"
#import "FirebaseMessagingPlugin.h"
#import <objc/runtime.h>

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
@import UserNotifications;

// Implement UNUserNotificationCenterDelegate to receive display notification via APNS for devices
// running iOS 10 and above. Implement FIRMessagingDelegate to receive data message via FCM for
// devices running iOS 10 and above.
@interface AppDelegate (FirebaseMessagingPlugin) <UNUserNotificationCenterDelegate>
@end
#endif

#define kApplicationInBackgroundKey @"applicationInBackground"

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

// [START ios_10_message_handling]
// Receive displayed notifications for iOS 10 devices.
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
// Handle incoming notification messages while app is in the foreground.
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    NSDictionary *userInfo = notification.request.content.userInfo;
    FirebaseMessagingPlugin* fmPlugin = [self.viewController getCommandInstance:@"FirebaseMessaging"];
    UNNotificationPresentationOptions options = UNNotificationPresentationOptionNone;

    [self postNotification:userInfo background:NO];
    // Change this to your preferred presentation option
    completionHandler(options);
}

// Handle notification messages after display notification is tapped by the user.
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)())completionHandler {
    NSDictionary *userInfo = response.notification.request.content.userInfo;

    [self postNotification:userInfo background:YES];

    completionHandler();
}
#endif
// [END ios_10_message_handling]

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    FirebaseMessagingPlugin* fmPlugin = [self.viewController getCommandInstance:@"FirebaseMessaging"];

    [fmPlugin registerNotifications:error];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    FirebaseMessagingPlugin* fmPlugin = [self.viewController getCommandInstance:@"FirebaseMessaging"];

    [fmPlugin registerNotifications:nil];
}

@end