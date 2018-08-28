package de.lohl1kohl.vsaapp.holder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.lohl1kohl.vsaapp.server.AGs;
import de.lohl1kohl.vsaapp.server.Callbacks.agsCallback;

public class AGsHolder {

    private static List<AG> ags = new ArrayList<>();


    public static void load(Context context, Callbacks.agsLoadedCallback agsLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // Show the old ags first (for a faster reaction time)...
        ags = getSavedAGs(context);
        if (agsLoadedCallback != null) agsLoadedCallback.onOldLoaded();


        agsCallback agsCallback = new agsCallback() {

            public void onReceived(String output) {
                ags = convertJsonToArray(output);

                // Save the current sp in the settings...
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pref_ags", output);
                editor.apply();

                if (agsLoadedCallback != null) agsLoadedCallback.onNewLoaded();
            }

            public void onConnectionFailed() {
                ags = getSavedAGs(context);
                if (agsLoadedCallback != null)
                    agsLoadedCallback.onConnectionFailed();
            }
        };

        // Send request to server...
        new AGs().getAGs(agsCallback);
    }

    @Nullable
    private static List<AG> getSavedAGs(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedAGs = sharedPref.getString("pref_ags", "-1");

        if (savedAGs.equals("-1")) {
            return new ArrayList<>();
        }
        return convertJsonToArray(savedAGs);
    }

    @Nullable
    private static List<AG> convertJsonToArray(String array) {
        List<AG> ags = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(array);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject day = jsonArray.getJSONObject(i);
                for (int j = 0; j < day.getJSONArray("ags").length(); j++) {
                    JSONObject g = day.getJSONArray("ags").getJSONObject(j);
                    ags.add(new AG(g.getString("name"), day.getString("weekday"), g.getString("time"), g.getString("room"), g.getString("grades")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VsaApp/AGsHolder", "Cannot convert JSONarray!");
            return null;
        }
        return ags;
    }

    public static List<AG> getFilteredAGs(Context context) {
        List<AG> ags = new ArrayList<>();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");
        try {
            grade = String.valueOf(Integer.valueOf(grade.substring(0, 1)));
        } catch (NumberFormatException ignored) {

        }
        for (AG ag : AGsHolder.ags) {
            if (ag.isAllowedToParticipate(grade)) {
                ags.add(ag);
            }
        }
        return ags;
    }

    public static class AG {
        public String name;
        public String weekday;
        public String time;
        public String room;
        public String grades;

        public AG(String name, String weekday, String time, String room, String grades) {
            this.name = name;
            this.weekday = weekday;
            this.time = time;
            this.room = room;
            this.grades = grades;
        }

        public boolean isAllowedToParticipate(String grade) {
            boolean allowed = false;
            String[] grades = new String[]{"5", "6", "7", "8", "9", "EF", "Q1", "Q2"};
            if (this.grades.contains(" - ")) {
                int minI = Arrays.asList(grades).indexOf(this.grades.split(" - ")[0]);
                int maxI = Arrays.asList(grades).indexOf(this.grades.split(" - ")[1]);
                int myI = Arrays.asList(grades).indexOf(grade);
                allowed = myI >= minI && myI <= maxI;
            } else if (this.grades.equals(grade)) {
                allowed = true;
            }
            return allowed;
        }
    }
}
