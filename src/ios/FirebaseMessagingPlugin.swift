import Foundation
import FirebaseCore
import FirebaseMessaging
import UserNotifications

@objc(FirebaseMessagingPlugin)
class FirebaseMessagingPlugin: CDVPlugin {

    private var notificationCallbackId: String?
    private var tokenRefreshCallbackId: String?
    private var backgroundNotificationCallbackId: String?
    private var lastNotification: [AnyHashable: Any]?
    private var forceShow: UNNotificationPresentationOptions = []

    override func pluginInitialize() {
        super.pluginInitialize()
        print("Starting Firebase Messaging plugin (Swift)")

        // Инициализация Firebase, если она еще не выполнена
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }

        Messaging.messaging().delegate = self
    }

    // MARK: - API методов

    @objc(requestPermission:)
    func requestPermission(command: CDVInvokedUrlCommand) {
        let options = command.arguments[0] as? [String: Any]
        let forceShowSetting = options?["forceShow"] as? Bool ?? false

        if forceShowSetting {
            self.forceShow = [.badge, .sound, .alert]
        } else {
            self.forceShow = []
        }

        let center = UNUserNotificationCenter.current()
        let authOptions: UNAuthorizationOptions = [.alert, .sound, .badge]

        center.requestAuthorization(options: authOptions) { granted, error in
            let result: CDVPluginResult
            if let error = error {
                result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription)
            } else if !granted {
                result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Notifications permission is not granted")
            } else {
                result = CDVPluginResult(status: CDVCommandStatus_OK)
            }
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }

        DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
        }
    }

    @objc(getToken:)
    func getToken(command: CDVInvokedUrlCommand) {
        let type = command.arguments[0] as? String ?? ""

        if type.isEmpty {
            // Получение FCM токена
            Messaging.messaging().token { token, error in
                let result: CDVPluginResult
                if let error = error {
                    result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription)
                } else {
                    result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: token)
                }
                self.commandDelegate.send(result, callbackId: command.callbackId)
            }
        } else if type.hasPrefix("apns-") {
            // Получение APNS токена
            if let apnsToken = Messaging.messaging().apnsToken {
                let result: CDVPluginResult
                if type == "apns-string" {
                    let hexToken = apnsToken.map { String(format: "%02.2hhx", $0) }.joined()
                    result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: hexToken)
                } else {
                    result = CDVPluginResult(status: CDVCommandStatus_OK, messageAsArrayBuffer: apnsToken)
                }
                self.commandDelegate.send(result, callbackId: command.callbackId)
            } else {
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: nil), callbackId: command.callbackId)
            }
        }
    }

    @objc(subscribe:)
    func subscribe(command: CDVInvokedUrlCommand) {
        let topic = String(describing: command.arguments[0])
        Messaging.messaging().subscribe(toTopic: topic) { error in
            let result = error == nil ? CDVPluginResult(status: CDVCommandStatus_OK) : CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error?.localizedDescription)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    // MARK: - Колбэки для JS
    @objc(onMessage:)
    func onMessage(command: CDVInvokedUrlCommand) {
        self.notificationCallbackId = command.callbackId
    }

    @objc(onTokenRefresh:)
    func onTokenRefresh(command: CDVInvokedUrlCommand) {
        self.tokenRefreshCallbackId = command.callbackId
    }
}

// MARK: - MessagingDelegate
extension FirebaseMessagingPlugin: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let callbackId = self.tokenRefreshCallbackId, let token = fcmToken {
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: token)
            result?.keepCallback = true
            self.commandDelegate.send(result, callbackId: callbackId)
        }
    }
}
