// swift-tools-version:5.9

import PackageDescription

if let firebaseSdkVersion = Bundle.main.object(forInfoDictionaryKey: "FirebaseSdkVersion") as? String {
    print("Firebase SDK vesion is: \(firebaseSdkVersion)")
}

let package = Package(
    name: "cordova-plugin-firebase-messaging",
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "cordova-plugin-firebase-messaging", targets: ["cordova-plugin-firebase-messaging"])
    ],
    dependencies: [
        .package(url: "https://github.com/apache/cordova-ios.git", branch: "master"),
        .package(url: "https://github.com/firebase/firebase-ios-sdk.git", from: firebaseSdkVersion)
    ],
    targets: [
        .target(
            name: "cordova-plugin-firebase-messaging",
            dependencies: [
                .product(name: "Cordova", package: "cordova-ios"),
                .product(name: "FirebaseMessaging", package: "firebase-ios-sdk")
            ],
            path: "src/ios",
            resources: [],
            publicHeadersPath: "."
        )
    ]
)
