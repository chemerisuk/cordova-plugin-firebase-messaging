package by.chemerisuk.cordova.firebase;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;


public class FirebaseMessagingPlugin extends CordovaPlugin {

    @Override
    protected void pluginInitialize() {
        FirebaseMessaging.getInstance().subscribeToTopic("android");
        FirebaseMessaging.getInstance().subscribeToTopic("all");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("subscribe")) {
            this.subscribe(callbackContext, args.getString(0));
            return true;
        } else if (action.equals("unsubscribe")) {
            this.unsubscribe(callbackContext, args.getString(0));
            return true;
        }

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        FirebaseMessagingPlugin.onNotificationOpen(intent.getExtras());
    }

    public static void onNotificationOpen(Bundle bundle) {
        // if (callbackContext != null && bundle != null) {
        //     JSONObject json = new JSONObject();
        //     Set<String> keys = bundle.keySet();
        //     for (String key : keys) {
        //         try {
        //             json.put(key, bundle.get(key));
        //         } catch (JSONException e) {
        //             callbackContext.error(e.getMessage());
        //             return;
        //         }
        //     }

        //     callbackContext.success(json);
        // }
    }


    private void subscribe(final CallbackContext callbackContext, final String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);

        callbackContext.success();
    }

    private void unsubscribe(final CallbackContext callbackContext, final String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);

        callbackContext.success();
    }
}
