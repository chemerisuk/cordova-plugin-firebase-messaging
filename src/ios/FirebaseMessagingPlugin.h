#import <Cordova/CDV.h>
#import "AppDelegate.h"

@interface FirebaseMessagingPlugin : CDVPlugin
+ (FirebaseMessagingPlugin *) firebasePlugin;
- (void)requestPermission:(CDVInvokedUrlCommand*)command;
- (void)setBadge:(CDVInvokedUrlCommand*)command;
- (void)getBadge:(CDVInvokedUrlCommand*)command;
- (void)subscribe:(CDVInvokedUrlCommand*)command;
- (void)unsubscribe:(CDVInvokedUrlCommand*)command;
- (void)onMessage:(CDVInvokedUrlCommand*)command;
- (void)onTokenRefresh:(CDVInvokedUrlCommand*)command;
- (void)sendNotification:(NSDictionary*)userInfo;
- (void)tokenRefreshNotification:(NSString*)token;

@property (nonatomic, copy) NSString *notificationCallbackId;
@property (nonatomic, copy) NSString *tokenRefreshCallbackId;
@property (nonatomic, retain) NSDictionary* lastNotification;

@end