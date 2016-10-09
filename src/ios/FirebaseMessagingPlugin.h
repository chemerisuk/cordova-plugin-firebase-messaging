#import <Cordova/CDV.h>

@interface FirebaseMessagingPlugin : CDVPlugin

- (void)subscribe:(CDVInvokedUrlCommand*)command;
- (void)unsubscribe:(CDVInvokedUrlCommand*)command;

@end
