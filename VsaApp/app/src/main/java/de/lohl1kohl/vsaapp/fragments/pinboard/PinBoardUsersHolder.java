package de.lohl1kohl.vsaapp.fragments.pinboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;
import de.lohl1kohl.vsaapp.loader.Callbacks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PinBoardUsersHolder {
    @SuppressLint("StaticFieldLeak")
    private static List<String> users = new ArrayList<>();

    public static void load(Context context, boolean update) {
        load(context, update, null);
    }

    public static void load(Context context, boolean update, Callbacks.baseLoadedCallback usersLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> savedUsers = getSavedUsers(context);

        if (update) {
            Callbacks.baseCallback usersCallback = new Callbacks.baseCallback() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onReceived(String output) {
                    PinBoardUsersHolder.users = convertJsonToArray(output);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("pref_pinboard_users", output);
                    editor.apply();
                    if (usersLoadedCallback != null) usersLoadedCallback.onLoaded();
                }

                @Override
                public void onConnectionFailed() {
                    if (savedUsers != null) {
                        users = savedUsers;
                    }

                    if (usersLoadedCallback != null)
                        usersLoadedCallback.onLoaded();
                }
            };
            new PinBoardUsers().getUsers(usersCallback);
        } else {
            if (savedUsers != null) {
                users = savedUsers;
            }
            if (usersLoadedCallback != null)
                usersLoadedCallback.onLoaded();
        }
    }

    private static List<String> convertJsonToArray(String array) {
        List<String> users = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONObject(array).getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                users.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return users;
    }

    private static List<String> getSavedUsers(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedUsers = sharedPref.getString("pref_pinboard_users", "-1");

        if (savedUsers.equals("-1")) return null;
        return convertJsonToArray(savedUsers);
    }

    public static List<String> getUsers() {
        return users;
    }

    public static boolean isFollowing(Context context, String user) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean("pref_pinboard_users_" + user, false);
    }

    public static void follow(Context context, String user, Callbacks.baseCallback callback) {
        if (!isFollowing(context, user)) {
            new PinBoardFollow().follow(user, new Callbacks.baseCallback() {
                @Override
                public void onReceived(String output) {
                    FirebaseMessaging.getInstance().subscribeToTopic("pinboard-" + user.replaceAll(" ", "_")).addOnCompleteListener(task -> {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                        sharedPref.edit().putBoolean("pref_pinboard_users_" + user, true).apply();
                        callback.onReceived(output);
                    });
                }

                @Override
                public void onConnectionFailed() {
                    callback.onConnectionFailed();
                }
            });
        }
    }

    public static void unfollow(Context context, String user, Callbacks.baseCallback callback) {
        if (isFollowing(context, user)) {
            new PinBoardUnfollow().unfollow(user, new Callbacks.baseCallback() {
                @Override
                public void onReceived(String output) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("pinboard-" + user.replaceAll(" ", "_")).addOnCompleteListener(task -> {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                        sharedPref.edit().putBoolean("pref_pinboard_users_" + user, false).apply();
                        callback.onReceived(output);
                    });
                }

                @Override
                public void onConnectionFailed() {
                    callback.onConnectionFailed();
                }
            });
        }
    }
}
