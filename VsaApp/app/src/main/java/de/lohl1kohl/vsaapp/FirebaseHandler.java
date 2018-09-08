package de.lohl1kohl.vsaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseHandler {

    public static void subscribe(final Context context, final String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(task -> {
            Log.i("Firebase", "Subscribing to " + topic + " " + (task.isSuccessful() ? "succeeded" : "failed"));
            if (task.isSuccessful()) {
                // Update subscribed topics
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("subscribed", sharedPref.getString("subscribed", "") + topic + ",");
                editor.apply();
            }
        });
    }

    public static void unsubscribeAll(Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Get all subscribed topics
        String[] topics = sharedPref.getString("subscribed", "").split(",");
        if (!topics[0].equals("")) {
            for (final String topic : topics) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
                    Log.i("Firebase", "Unsubscribing from " + topic + " " + (task.isSuccessful() ? "succeeded" : "failed"));
                    if (task.isSuccessful()) {
                        // Remove topic from subscribed
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("subscribed", sharedPref.getString("subscribed", "").replace(topic + ",", ""));
                        editor.apply();
                    }
                });
            }
        }
    }
}
