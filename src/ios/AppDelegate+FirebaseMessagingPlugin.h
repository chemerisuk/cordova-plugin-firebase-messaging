#import "AppDelegate.h"

@import Firebase;

@interface AppDelegate (FirebaseMessagingPlugin) <FIRMessagingDelegate>

@property (nonatomic, strong) NSNumber *applicationInBackground;

@end