package de.lohl1kohl.vsaapp.fragments.cafetoria;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.lohl1kohl.vsaapp.loader.Callbacks;

public class CafetoriaHolder {

    public static List<Day> days = new ArrayList<>();

    public static void load(Context context, String id, String password, Callbacks.baseLoadedCallback cafetoriaLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // Show the old days first (for a faster reaction time)...
        days = getSavedDays(context);
        if (cafetoriaLoadedCallback != null) cafetoriaLoadedCallback.onOldLoaded();

        Callbacks.baseCallback cafetoriaCallback = new Callbacks.baseCallback() {

            public void onReceived(String output) {
                days = convertJsonToArray(output);

                // Save the current sp in the settings...
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pref_cafetoria", output);
                editor.apply();

                if (cafetoriaLoadedCallback != null) cafetoriaLoadedCallback.onNewLoaded();
            }

            public void onConnectionFailed() {
                days = getSavedDays(context);
                if (cafetoriaLoadedCallback != null)
                    cafetoriaLoadedCallback.onConnectionFailed();
            }
        };

        // Send request to server...
        new Cafetoria().updateMenues(id, password, cafetoriaCallback);
    }

    @Nullable
    private static List<Day> getSavedDays(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedDays = sharedPref.getString("pref_cafetoria", "-1");

        if (savedDays.equals("-1")) {
            return new ArrayList<>();
        }
        return convertJsonToArray(savedDays);
    }

    @Nullable
    private static List<Day> convertJsonToArray(String array) {
        Log.i("array", array);
        List<Day> days = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONObject(array).getJSONArray("menues");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String weekday = jsonObject.getString("weekday");
                String date = jsonObject.getString("date");
                List<Menu> menues = new ArrayList<>();
                for (int j = 0; j < jsonObject.getJSONArray("menues").length(); j++) {
                    JSONObject menu = jsonObject.getJSONArray("menues").getJSONObject(j);
                    String startTime = menu.getJSONObject("time").getString("start");
                    String endTime = "";
                    try {
                        endTime = menu.getJSONObject("time").getString("end");
                    } catch (JSONException ignored) {

                    }
                    String food = menu.getString("food");
                    double price = menu.getDouble("price");
                    menues.add(new Menu(food, price, startTime, endTime));
                }
                Extra extra = new Extra(jsonObject.getJSONObject("extra").getString("food"), jsonObject.getJSONObject("extra").getDouble("price"), jsonObject.getJSONObject("extra").getString("time"));
                Snack snack = new Snack(jsonObject.getJSONObject("snack").getString("food"), jsonObject.getJSONObject("snack").getDouble("price"), jsonObject.getJSONObject("snack").getString("time"));
                days.add(new Day(weekday, date, menues.get(0), menues.get(1), extra, snack));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VsaApp/CafetoriaHolder", "Cannot convert JSONarray!");
            return null;
        }
        return days;
    }

    public static class Menu {
        public String food;
        public double price;
        public String startTime;
        public String endTime;

        public Menu(String food, double price, String startTime, String endTime) {
            this.food = food;
            this.price = price;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public static class Extra extends Menu {

        public Extra(String food, double price, String time) {
            super(food, price, time, "");
        }
    }

    public static class Day {
        public Menu menu1;
        public Menu menu2;
        public Extra extra;
        public Snack snack;
        private String weekday;
        private String date;

        public Day(String weekday, String date, Menu menu1, Menu menu2, Extra extra, Snack snack) {
            this.weekday = weekday;
            this.date = date;
            this.menu1 = menu1;
            this.menu2 = menu2;
            this.extra = extra;
            this.snack = snack;
        }
    }

    public static class Snack extends Menu {

        public Snack(String food, double price, String time) {
            super(food, price, time, "");
        }
    }
}
