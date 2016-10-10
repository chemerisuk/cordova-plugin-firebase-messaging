var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseMessagingPlugin";

module.exports = {
    subscribe: function(topic, success, error) {
        exec(success, error, PLUGIN_NAME, "subscribe", [topic]);
    },
    unsubscribe: function(topic, success, error) {
        exec(success, error, PLUGIN_NAME, "unsubscribe", [topic]);
    },
    registerTokenReceiver: function(success, error) {
        exec(success, error, PLUGIN_NAME, "registerTokenReceiver", []);
    },
    registerMessageReceiver: function(success, error) {
        exec(success, error, PLUGIN_NAME, "registerMessageReceiver", []);
    },
    setBadgeNumber: function(value, success, error) {
        exec(success, error, PLUGIN_NAME, "setBadgeNumber", [value]);
    },
    getBadgeNumber: function(success, error) {
        exec(success, error, PLUGIN_NAME, "getBadgeNumber", []);
    },
    grantPermission: function(success, error) {
        exec(success, error, PLUGIN_NAME, "grantPermission", []);
    }
};
