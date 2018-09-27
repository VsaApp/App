package de.lohl1kohl.vsaapp.holders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.lohl1kohl.vsaapp.loader.Callbacks;
import de.lohl1kohl.vsaapp.loader.Sums;

public class SumsHolder {

    private static Map<String, String> sums = new HashMap<>();
    private static Map<String, String> oldSums = new HashMap<>();

    public static void load(Context context, Callbacks.baseLoadedCallback sumsLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        oldSums = getSavedSums(context);
        Callbacks.baseCallback sumsCallback = new Callbacks.baseCallback() {
            @Override
            public void onReceived(String output) {
                sums = convertJsonToArray(output);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pref_sums", output);
                editor.apply();

                if (sumsLoadedCallback != null) sumsLoadedCallback.onLoaded();
            }

            @Override
            public void onConnectionFailed() {
                sums = getSavedSums(context);
                if (sumsLoadedCallback != null)
                    sumsLoadedCallback.onLoaded();
            }
        };
        new Sums().getSums(sumsCallback);
    }

    public static boolean isLoaded(){
        return sums.size() > 0;
    }

    public static Map<String, String> getSavedSums(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedSums = sharedPref.getString("pref_sums", "-1");

        if (savedSums.equals("-1")) {
            return new HashMap<>();
        }
        return convertJsonToArray(savedSums);
    }

    private static Map<String, String> convertJsonToArray(String array) {
        Map<String, String> sums = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(array);
            Iterator<String> nameItr = jsonObject.keys();
            while (nameItr.hasNext()) {
                String name = nameItr.next();
                sums.put(name, jsonObject.getString(name));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VsaApp/SumsHolder", "Cannot convert JSONarray!");
            return null;
        }
        return sums;
    }

    public static Map<String, Boolean> getChangedSums() {
        Map<String, Boolean> changed = new HashMap<>();
        if (oldSums != sums) {
            for (Map.Entry<String, String> entry : sums.entrySet()) {
                if (!oldSums.containsKey(entry.getKey())) {
                    changed.put(entry.getKey(), true);
                } else if (!entry.getValue().equals(oldSums.get(entry.getKey()))) {
                    changed.put(entry.getKey(), true);
                } else {
                    changed.put(entry.getKey(), false);
                }
            }
        }
        return changed;
    }
}
