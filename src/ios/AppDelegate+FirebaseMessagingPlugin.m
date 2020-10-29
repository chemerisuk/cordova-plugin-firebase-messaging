#import "AppDelegate+FirebaseMessagingPlugin.h"
#import "FirebaseMessagingPlugin.h"

@implementation AppDelegate (FirebaseMessagingPlugin)

- (FirebaseMessagingPlugin*) getPluginInstance {
    return [self.viewController getCommandInstance:@"FirebaseMessaging"];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    FirebaseMessagingPlugin* fmPlugin = [self getPluginInstance];
    if (application.applicationState != UIApplicationStateActive) {
        [fmPlugin sendBackgroundNotification:userInfo];
    } else {
        [fmPlugin sendNotification:userInfo];
    }

    completionHandler(UIBackgroundFetchResultNewData);
}

- (void)messaging:(FIRMessaging *)messaging didReceiveRegistrationToken:(NSString *)fcmToken {
    FirebaseMessagingPlugin* fmPlugin = [self getPluginInstance];
    [fmPlugin sendToken:fcmToken];
}

@end
