package de.lohl1kohl.vsaapp.holder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.lohl1kohl.vsaapp.Date;
import de.lohl1kohl.vsaapp.Event;
import de.lohl1kohl.vsaapp.server.Dates;

public class DatesHolder {
    private static List<Event> events;
    private static List<List<Event>> calendar;

    public static void load(Context context) {
        load(context, null);
    }

    public static void load(Context context, Callbacks.datesLoadedCallback datesLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // If the dates are already loaded stop process...
        if (events != null) {
            if (datesLoadedCallback != null) datesLoadedCallback.onFinished();
            return;
        }

        // Show the old dates first (for a faster reaction time)...
        readSavedDates(context);
        if (datesLoadedCallback != null) datesLoadedCallback.onFinished();

        de.lohl1kohl.vsaapp.server.Callbacks.datesCallback datesCallback = new de.lohl1kohl.vsaapp.server.Callbacks.datesCallback() {

            public void onReceived(String output) {
                convertJson(context, output);

                // Save the current sp in the settings...
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pref_dates", output);
                editor.apply();

                if (datesLoadedCallback != null) datesLoadedCallback.onFinished();
            }

            public void onConnectionFailed() {
                Log.e("VsaApp/SpHolder", "No connection!");
                readSavedDates(context);
                if (datesLoadedCallback != null) datesLoadedCallback.onConnectionFailed();
            }
        };

        // Send request to server...
        new Dates().updateDates(datesCallback);
    }

    private static void readSavedDates(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedSP = sharedPref.getString("pref_dates", "-1");

        if (!savedSP.equals("-1")) convertJson(context, savedSP);

    }

    private static void convertJson(Context context, String array) {
        try {
            JSONObject jsonObject = new JSONObject(array);
            JSONArray holidaysO = jsonObject.getJSONArray("holidays");
            JSONObject openDoorDayO = jsonObject.getJSONObject("openDoorDay");
            JSONArray freeDaysO = jsonObject.getJSONArray("freeDays");
            JSONArray consultationDaysO = jsonObject.getJSONArray("consultationDays");
            JSONArray conferencesO = jsonObject.getJSONArray("conferences");
            JSONArray gradesReleasesO = jsonObject.getJSONArray("gradesReleases");

            events = new ArrayList<Event>();

            // Convert open door day...
            Date date = new Date(openDoorDayO.getInt("day"), openDoorDayO.getString("month"), openDoorDayO.getInt("year"));
            events.add(new Event(openDoorDayO.getString("description"), date, date));

            // Convert holidays...
            for (int i = 0; i < holidaysO.length(); i++) {
                JSONObject holdiayObject = holidaysO.getJSONObject(i);
                Date start = new Date(holdiayObject.getJSONObject("start").getString("weekday"), holdiayObject.getJSONObject("start").getInt("day"), holdiayObject.getJSONObject("start").getString("month"), holdiayObject.getJSONObject("start").getInt("year"));
                Date end = new Date(holdiayObject.getJSONObject("end").getString("weekday"), holdiayObject.getJSONObject("end").getInt("day"), holdiayObject.getJSONObject("end").getString("month"), holdiayObject.getJSONObject("end").getInt("year"));
                events.add(new Event(holdiayObject.getString("name"), "holidays", start, end));
            }

            // Convert free days...
            for (int i = 0; i < freeDaysO.length(); i++) {
                JSONObject freeDateObject = freeDaysO.getJSONObject(i);
                date = new Date(freeDateObject.getString("weekday"), freeDateObject.getInt("day"), freeDateObject.getString("month"), freeDateObject.getInt("year"));
                events.add(new Event(freeDateObject.getString("description"), "freeDate", date, date));
            }

            // Convert consultation days...
            for (int i = 0; i < consultationDaysO.length(); i++) {
                JSONObject consultationDayObject = consultationDaysO.getJSONObject(i);
                Date start = new Date(consultationDayObject.getString("weekday"), consultationDayObject.getInt("day"), consultationDayObject.getString("month"), consultationDayObject.getInt("year"));
                Date end = new Date(consultationDayObject.getString("weekday"), consultationDayObject.getInt("day"), consultationDayObject.getString("month"), consultationDayObject.getInt("year"));
                String time = consultationDayObject.getString("time");
                if (time.split(" - ").length == 2) {
                    int startTime = Integer.parseInt(time.split(" - ")[0]);
                    int endTime = Integer.parseInt(time.split(" - ")[1]);
                    start.setTime(0, startTime);
                    end.setTime(0, endTime);
                } else end = start;
                events.add(new Event(consultationDayObject.getString("description"), "consultationDay", start, end));
            }

            // Convert conferences...
            for (int i = 0; i < conferencesO.length(); i++) {
                JSONObject conferenceObject = conferencesO.getJSONObject(i);
                JSONObject day = conferenceObject.getJSONObject("day");
                Date start = new Date(day.getString("weekday"), day.getInt("day"), day.getString("month"), day.getInt("year"));
                JSONArray grades = conferenceObject.getJSONArray("grade");
                for (int j = 0; j < grades.length(); j++) {
                    start.addGrade(grades.getString(j));
                }
                events.add(new Event(conferenceObject.getString("description"), "conference", start, start));
            }

            // Convert grades releases...
            for (int i = 0; i < gradesReleasesO.length(); i++) {
                JSONObject gradesReleaseObject = gradesReleasesO.getJSONObject(i);
                JSONObject day = gradesReleaseObject.getJSONObject("day");
                Date start = new Date(day.getString("weekday"), day.getInt("day"), day.getString("month"), day.getInt("year"));
                Date end = new Date(day.getString("weekday"), day.getInt("day"), day.getString("month"), day.getInt("year"));
                start.setTime(0, 8);
                end.setTime(0, Integer.parseInt(gradesReleaseObject.getString("schoolOff").split(" ")[1]));
                events.add(new Event(gradesReleaseObject.getString("description"), "gradeReleas", start, end));
            }

            sortEvents(context);
            createCalendar(context);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VsaApp/DatesHolder", "Cannot convert JSONarray!");
        }
    }

    private static void sortEvents(Context c) {
        List<Event> sortedList = new ArrayList<Event>();
        sortedList.add(events.get(0));
        for (int i = 1; i < events.size(); i++) {
            Date date = events.get(i).start;
            int j = 0;
            for (j = 0; j < sortedList.size(); j++) {
                if (date.getTimestamp(c) <= sortedList.get(j).start.getTimestamp(c)) break;
            }
            sortedList.add(j, events.get(i));
        }
        events = sortedList;
    }

    public static void createCalendar(Context c) {
        calendar = new ArrayList<>();

        java.util.Date date = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Date today = new Date(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));

        for (int i = 1; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.end.getTimestamp(c) < today.getTimestamp(c)) continue;
            if (calendar.size() > 0) {
                if (event.start.getYear() == calendar.get(calendar.size() - 1).get(0).start.getYear()) {
                    if (event.start.getMonth(c) == calendar.get(calendar.size() - 1).get(0).start.getMonth(c)) {
                        if (event.start.getDay() == calendar.get(calendar.size() - 1).get(0).start.getDay()) {
                            calendar.get(calendar.size() - 1).add(event);
                            continue;
                        }
                    }
                }
            }
            List<Event> day = new ArrayList<Event>();
            day.add(event);
            calendar.add(day);
        }
    }

    public static List<Event> getEvents() {
        return events;
    }

    public static List<List<Event>> getCalendar() {
        return calendar;
    }
}
