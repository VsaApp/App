package de.lohl1kohl.vsaapp.fragments.pinboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.lohl1kohl.vsaapp.loader.Callbacks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinBoardMessagesHolder {
    @SuppressLint("StaticFieldLeak")
    private static Map<String, List<Message>> messages = new HashMap<>();

    public static void load(Context context, String user, boolean update) {
        load(context, user, update, null);
    }

    public static void load(Context context, String user, boolean update, Callbacks.baseLoadedCallback messagesLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        List<Message> savedMessages = getSavedMessages(context, user);

        if (update) {
            Callbacks.baseCallback messagesCallback = new Callbacks.baseCallback() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onReceived(String output) {
                    PinBoardMessagesHolder.messages.put(user, convertJsonToArray(output));
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("pref_pinboard_messages_" + user, output);
                    editor.apply();
                    if (messagesLoadedCallback != null) messagesLoadedCallback.onLoaded();
                }

                @Override
                public void onConnectionFailed() {
                    if (savedMessages != null) {
                        messages.put(user, savedMessages);
                    }

                    if (messagesLoadedCallback != null)
                        messagesLoadedCallback.onLoaded();
                }
            };
            new PinBoardMessages().getMessages(user, messagesCallback);
        } else {
            if (savedMessages != null) {
                messages.put(user, savedMessages);
            }
            if (messagesLoadedCallback != null)
                messagesLoadedCallback.onLoaded();
        }
    }

    private static List<Message> convertJsonToArray(String array) {
        List<Message> messages = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONObject(array).getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                messages.add(new Message(jsonArray.getJSONObject(i).getString("title"), jsonArray.getJSONObject(i).getString("message"), jsonArray.getJSONObject(i).getLong("time")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messages;
    }

    private static List<Message> getSavedMessages(Context context, String user) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedMessages = sharedPref.getString("pref_pinboard_messages_" + user, "-1");

        if (savedMessages.equals("-1")) return null;
        return convertJsonToArray(savedMessages);
    }

    public static List<Message> getMessages(String user) {
        return messages.get(user);
    }

    static class Message {
        private String title;
        private String message;
        private Long time;

        public Message(String title, String message, Long time) {
            this.title = title;
            this.message = message;
            this.time = time;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public Long getTime() {
            return time;
        }
    }
}
