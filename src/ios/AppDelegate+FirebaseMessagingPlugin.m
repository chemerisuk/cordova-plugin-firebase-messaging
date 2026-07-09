#import "AppDelegate+FirebaseMessagingPlugin.h"
#import "FirebaseMessagingPlugin.h"
#import <objc/runtime.h>

@implementation CDVAppDelegate (FirebaseMessagingPlugin)

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
    // always call original method implementation first
    BOOL handled = [self identity_application:application didFinishLaunchingWithOptions:launchOptions];

    [UNUserNotificationCenter currentNotificationCenter].delegate = self;

//    if (launchOptions) {
//        NSDictionary *userInfo = launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey];
//        if (userInfo) {
//            [self postNotification:userInfo background:TRUE];
//        }
//    }

    return handled;
}

- (FirebaseMessagingPlugin*) getPluginInstance {
    return [self.viewController getCommandInstance:@"FirebaseMessaging"];
}

// Unified Helper to call Super
static void firebase_execute_fwd(id self, Class superClass, SEL selector, void(^fwdBlock)(IMP superMethodImp)) {
    if ([superClass instancesRespondToSelector:selector]) {
        fwdBlock([superClass instanceMethodForSelector:selector]);
    } else {
        fwdBlock(NULL);
    }
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    // 1. Thread-safe block wrapper to prevent duplicate completionHandler execution.
    // If Firebase SDK auto-proxy and this plugin both call it, the app won't crash.
    __block BOOL completionHandlerCalled = NO;
    void (^safeCompletionHandler)(UIBackgroundFetchResult) = ^(UIBackgroundFetchResult result) {
        if (!completionHandlerCalled) {
            completionHandlerCalled = YES;
            completionHandler(result);
        }
    };

    // 2. Process notification payload within the plugin
    FirebaseMessagingPlugin* fcmPlugin = [self getPluginInstance];
    if (application.applicationState != UIApplicationStateActive) {
        [fcmPlugin sendBackgroundNotification:userInfo];
    } else {
        [fcmPlugin sendNotification:userInfo];
    }

    firebase_execute_fwd(self, [super class], _cmd, ^(IMP superMethodImp) {
        if (superMethodImp) {
            void (*fwdCall)(id, SEL, UIApplication *, NSDictionary *, void (^)(UIBackgroundFetchResult)) = (void *)superMethodImp;
            fwdCall(self, _cmd, application, userInfo, safeCompletionHandler);
        } else {
            safeCompletionHandler(UIBackgroundFetchResultNewData);
        }
    });
}

# pragma mark - UNUserNotificationCenterDelegate
// handle incoming notification messages while app is in the foreground
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    // 1. Safe completion handler wrapper to prevent double invocation
    __block BOOL completionHandlerCalled = NO;
    void (^safeCompletionHandler)(UNNotificationPresentationOptions) = ^(UNNotificationPresentationOptions options) {
        if (!completionHandlerCalled) {
            completionHandlerCalled = YES;
            completionHandler(options);
        }
    };

    // 2. Pass notification to the plugin
    FirebaseMessagingPlugin* fcmPlugin = [self getPluginInstance];
    [fcmPlugin sendNotification:notification.request.content.userInfo];

    firebase_execute_fwd(self, [super class], _cmd, ^(IMP superMethodImp) {
        if (superMethodImp) {
            ((void (*)(id, SEL, UNUserNotificationCenter *, UNNotification *, void (^)(UNNotificationPresentationOptions)))superMethodImp)(self, _cmd, center, notification, safeCompletionHandler);
        } else {
            safeCompletionHandler(fcmPlugin.forceShow);
        }
    });
}

// handle notification messages after display notification is tapped by the user
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    // 1. Thread-safe block to prevent duplicate completionHandler execution
    __block BOOL completionHandlerCalled = NO;
    void (^safeCompletionHandler)(void) = ^{
        if (!completionHandlerCalled) {
            completionHandlerCalled = YES;
            completionHandler();
        }
    };

    // 2. Forward payload to plugin
    FirebaseMessagingPlugin* fcmPlugin = [self getPluginInstance];
    [fcmPlugin sendBackgroundNotification:response.notification.request.content.userInfo];

    // 3. Forward to super (handles potential conflicts with other plugins)
    firebase_execute_fwd(self, [super class], _cmd, ^(IMP superMethodImp) {
        if (superMethodImp) {
            void (*fwdCall)(id, SEL, UNUserNotificationCenter *, UNNotificationResponse *, void (^)(void)) = (void *)superMethodImp;
            fwdCall(self, _cmd, center, response, safeCompletionHandler);
        } else {
            safeCompletionHandler();
        }
    });
}

@end
