interface CordovaPlugins {
    firebase: FirebasePlugins;
}

interface FirebasePlugins {
    messaging: typeof import("./FirebaseMessaging");
}
