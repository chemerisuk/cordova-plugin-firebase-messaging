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

    // 3. Clean approach to forward the call to the original implementation (super)
    // This dynamically fetches the original IMP to bypass performSelector limitations with 3 arguments.
    SEL originalSelector = @selector(application:didReceiveRemoteNotification:fetchCompletionHandler:);

    if ([super respondsToSelector:originalSelector]) {
        // Get the implementation of the super class method directly
        IMP superMethodImp = [super methodForSelector:originalSelector];

        // Cast the implementation function pointer to match the method signature
        void (*fwdCall)(id, SEL, UIApplication *, NSDictionary *, void (^)(UIBackgroundFetchResult)) = (void *)superMethodImp;

        // Execute the original method implementation seamlessly
        fwdCall(self, originalSelector, application, userInfo, safeCompletionHandler);
    } else {
        // Fallback fallback if no other plugin or main AppDelegate implements this method
        safeCompletionHandler(UIBackgroundFetchResultNewData);
    }
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
    NSDictionary *userInfo = notification.request.content.userInfo;
    FirebaseMessagingPlugin* fcmPlugin = [self getPluginInstance];

    [fcmPlugin sendNotification:userInfo];

    // Default options from configuration
    UNNotificationPresentationOptions defaultOptions = [self getPluginInstance].forceShow;

    // 3. Forward the call to preserve chain of responsibility
    SEL originalSelector = @selector(userNotificationCenter:willPresentNotification:withCompletionHandler:);
    if ([super respondsToSelector:originalSelector]) {
        IMP superMethodImp = [super methodForSelector:originalSelector];
        void (*fwdCall)(id, SEL, UNUserNotificationCenter *, UNNotification *, void (^)(UNNotificationPresentationOptions)) = (void *)superMethodImp;
        fwdCall(self, originalSelector, center, notification, safeCompletionHandler);
    } else {
        safeCompletionHandler(defaultOptions);
    }
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
    NSDictionary *userInfo = response.notification.request.content.userInfo;
    FirebaseMessagingPlugin* fcmPlugin = [self getPluginInstance];
    if (fcmPlugin) {
        [fcmPlugin sendBackgroundNotification:userInfo];
    }

    // 3. Forward to super (handles potential conflicts with other plugins)
    SEL selector = @selector(userNotificationCenter:didReceiveNotificationResponse:withCompletionHandler:);
    if ([super respondsToSelector:selector]) {
        void (*superImp)(id, SEL, UNUserNotificationCenter *, UNNotificationResponse *, void (^)(void)) = (void *)[super methodForSelector:selector];
        superImp(self, selector, center, response, safeCompletionHandler);
    } else {
        safeCompletionHandler();
    }
}

@end
