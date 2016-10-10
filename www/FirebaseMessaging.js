var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseMessagingPlugin";

module.exports = {
    subscribe: function(topic, success, error) {
        exec(success, error, PLUGIN_NAME, "subscribe", [topic]);
    },
    unsubscribe: function(topic, success, error) {
        exec(success, error, PLUGIN_NAME, "unsubscribe", [topic]);
    },
    getToken: function(success, error) {
        exec(success, error, PLUGIN_NAME, "getToken", []);
    },
    handleNotification: function(success, error) {
        exec(success, error, PLUGIN_NAME, "handleNotification", []);
    }
};
