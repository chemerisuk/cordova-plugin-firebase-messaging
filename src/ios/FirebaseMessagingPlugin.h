#import <Cordova/CDV.h>
#import "AppDelegate.h"

@interface FirebaseMessagingPlugin : CDVPlugin
+ (FirebaseMessagingPlugin *) firebasePlugin;
- (void)grantPermission:(CDVInvokedUrlCommand*)command;
- (void)setBadgeNumber:(CDVInvokedUrlCommand*)command;
- (void)getBadgeNumber:(CDVInvokedUrlCommand*)command;
- (void)subscribe:(CDVInvokedUrlCommand*)command;
- (void)unsubscribe:(CDVInvokedUrlCommand*)command;
- (void)onMessage:(CDVInvokedUrlCommand*)command;
- (void)onToken:(CDVInvokedUrlCommand*)command;
- (void)sendNotification:(NSDictionary*)userInfo;
- (void)tokenRefreshNotification:(NSString*)token;

@property (nonatomic, copy) NSString *notificationCallbackId;
@property (nonatomic, copy) NSString *tokenRefreshCallbackId;
@property (nonatomic, retain) NSMutableArray *notificationStack;

@end