// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "cordova-plugin-firebase-messaging",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "cordova-plugin-firebase-messaging", targets: ["cordova-plugin-firebase-messaging"])
    ],
    dependencies: [
        .package(url: "https://github.com", from: "11.0.0")
    ],
    targets: [
        .target(
            name: "cordova-plugin-firebase-messaging",
            dependencies: [
                .product(name: "FirebaseMessaging", package: "firebase-ios-sdk")
            ],
            path: "src/ios"
        )
    ]
)
