package de.lohl1kohl.vsaapp.holders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.calendar.Date;
import de.lohl1kohl.vsaapp.fragments.calendar.Event;
import de.lohl1kohl.vsaapp.fragments.calendar.Holidays;
import de.lohl1kohl.vsaapp.loader.Callbacks;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class HolidayHolder {

    private static List<Event> holidays;
    private static int loadedYears;

    public static void load(Context context, Callbacks.baseLoadedCallback holidaysLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar now = Calendar.getInstance();
        now.setTime(new java.util.Date());
        loadedYears = 0;
        holidays = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            int year = now.get(Calendar.YEAR) + i;
            if (sharedPref.getString(String.format(Locale.GERMAN, "holidays_%d", now.get(Calendar.YEAR) + i), "-1").equals("-1")) {
                Callbacks.baseCallback hoidaysCallback = new Callbacks.baseCallback() {

                    public void onReceived(String output) {
                        convertHolidaysJson(context, output);

                        // Save the current sp in the settings...
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(String.format(Locale.GERMAN, "holidays_%d", year), output);
                        editor.apply();

                        loadedYears++;

                        if (holidaysLoadedCallback != null && loadedYears == 2)
                            holidaysLoadedCallback.onLoaded();
                    }

                    public void onConnectionFailed() {
                        loadSavedHolidaysForYear(context, year);
                        loadedYears++;
                        if (holidaysLoadedCallback != null && loadedYears == 2)
                            holidaysLoadedCallback.onLoaded();
                    }
                };

                // Send request to server...
                new Holidays().updateDates(hoidaysCallback, year);
            } else {
                loadSavedHolidaysForYear(context, year);
                loadedYears++;
                if (holidaysLoadedCallback != null && loadedYears == 2)
                    holidaysLoadedCallback.onLoaded();
            }
        }
    }

    private static void convertHolidaysJson(Context context, String array) {
        try {
            JSONObject jsonObject = new JSONObject(array);
            Iterator allKeys = jsonObject.keys();
            while (allKeys.hasNext()) {
                String name = (String) allKeys.next();
                JSONObject holiday = jsonObject.getJSONObject(name);
                String date = holiday.getString("datum");
                String info = holiday.getString("hinweis");
                Date day = new Date(Integer.parseInt(date.split("-")[2]), Integer.parseInt(date.split("-")[1]), Integer.parseInt(date.split("-")[0]));
                Event event = new Event(name, info, day, day, DatesHolder.getCategory(context.getString(R.string.holiday_category)));
                holidays.add(event);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VsaApp/DatesHolder", "Cannot convert holidays JSONarray!");
        }
    }

    private static void loadSavedHolidaysForYear(Context context, int year) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String output = sharedPref.getString(String.format(Locale.GERMAN, "holidays_%d", year), "");

        convertHolidaysJson(context, output);
    }

    public static List<Event> getHolidays() {
        return holidays;
    }

}
