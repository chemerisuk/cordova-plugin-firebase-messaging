#import "AppDelegate+FirebaseMessagingPlugin.h"
#import "FirebaseMessagingPlugin.h"
#import <objc/runtime.h>

@implementation AppDelegate (FirebaseMessagingPlugin)

// Borrowed from http://nshipster.com/method-swizzling/
+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class class = [self class];

        SEL originalSelector = @selector(application:didFinishLaunchingWithOptions:);
        SEL swizzledSelector = @selector(identity_application:didFinishLaunchingWithOptions:);

        Method originalMethod = class_getInstanceMethod(class, originalSelector);
        Method swizzledMethod = class_getInstanceMethod(class, swizzledSelector);

        BOOL didAddMethod =
        class_addMethod(class,
                        originalSelector,
                        method_getImplementation(swizzledMethod),
                        method_getTypeEncoding(swizzledMethod));

        if (didAddMethod) {
            class_replaceMethod(class,
                                swizzledSelector,
                                method_getImplementation(originalMethod),
                                method_getTypeEncoding(originalMethod));
        } else {
            method_exchangeImplementations(originalMethod, swizzledMethod);
        }
    });
}

- (BOOL)identity_application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    [FIRMessaging messaging].delegate = self;
    [UNUserNotificationCenter currentNotificationCenter].delegate = self;

    if (launchOptions) {
        NSDictionary *userInfo = launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey];
        if (userInfo) {
            [self postNotification:userInfo background:TRUE];
        }
    }

    return [self identity_application:application didFinishLaunchingWithOptions:launchOptions];
}

- (FirebaseMessagingPlugin*) getPluginInstance {
    return [self.viewController getCommandInstance:@"FirebaseMessaging"];
}

- (void)postNotification:(NSDictionary*)userInfo background:(BOOL)background {
    FirebaseMessagingPlugin* fmPlugin = [self getPluginInstance];
    if (background) {
        [fmPlugin sendBackgroundNotification:userInfo];
    } else {
        [fmPlugin sendNotification:userInfo];
    }
}

// handle incoming notification messages while app is in the background
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    BOOL value = application.applicationState != UIApplicationStateActive;

    [self postNotification:userInfo background:value];

    completionHandler(UIBackgroundFetchResultNewData);
}

# pragma mark - UNUserNotificationCenterDelegate
// handle incoming notification messages while app is in the foreground
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    NSDictionary *userInfo = notification.request.content.userInfo;

    [self postNotification:userInfo background:FALSE];
    // Change this to your preferred presentation option
    completionHandler([self getPluginInstance].forceShow);
}

// handle notification messages after display notification is tapped by the user
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    NSDictionary *userInfo = response.notification.request.content.userInfo;

    [self postNotification:userInfo background:TRUE];

    completionHandler();
}

# pragma mark - FIRMessagingDelegate

- (void)messaging:(FIRMessaging *)messaging didReceiveRegistrationToken:(NSString *)fcmToken {
    [[self getPluginInstance] sendToken:fcmToken];
}

@end
