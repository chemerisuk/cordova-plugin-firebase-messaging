var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseMessaging";

module.exports = {
    subscribe: function(topic, success, error) {
        exec(success, error, PLUGIN_NAME, "subscribe", [topic]);
    },
    unsubscribe: function(topic, success, error) {
        exec(success, error, PLUGIN_NAME, "unsubscribe", [topic]);
    },
    onTokenRefresh: function(success, error) {
        exec(success, error, PLUGIN_NAME, "onTokenRefresh", []);
    },
    onMessage: function(success, error) {
        exec(success, error, PLUGIN_NAME, "onMessage", []);
    },
    setBadge: function(value, success, error) {
        exec(success, error, PLUGIN_NAME, "setBadge", [value]);
    },
    getBadge: function(success, error) {
        exec(success, error, PLUGIN_NAME, "getBadge", []);
    },
    requestPermission: function(success, error) {
        exec(success, error, PLUGIN_NAME, "requestPermission", []);
    }
};
