package de.lohl1kohl.vsaapp.holders;

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
import java.util.Locale;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.calendar.Category;
import de.lohl1kohl.vsaapp.fragments.calendar.Color;
import de.lohl1kohl.vsaapp.fragments.calendar.Date;
import de.lohl1kohl.vsaapp.fragments.calendar.Dates;
import de.lohl1kohl.vsaapp.fragments.calendar.Day;
import de.lohl1kohl.vsaapp.fragments.calendar.Event;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class DatesHolder {
    private static List<Event> events;
    private static List<Event> customEvents;
    private static List<Day> calendar;
    private static List<Category> categories;

    public static void load(Context context, boolean update) {
        load(context, update, null);
    }

    public static void load(Context context, boolean update, Callbacks.baseLoadedCallback datesLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        loadCategories(context);

        if (update) {

            Callbacks.baseCallback datesCallback = new Callbacks.baseCallback() {

                public void onReceived(String output) {
                    HolidayHolder.load(context, () -> {
                        convertJson(context, output);
                        events.addAll(HolidayHolder.getHolidays());
                        sortEvents(context);
                        createCalendar(context);

                        // Save the current sp in the settings...
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("pref_dates", output);
                        editor.apply();

                        if (datesLoadedCallback != null) datesLoadedCallback.onLoaded();
                    });
                }

                public void onConnectionFailed() {
                    readSavedDates(context);
                    events.addAll(HolidayHolder.getHolidays());
                    sortEvents(context);
                    createCalendar(context);
                    if (datesLoadedCallback != null) datesLoadedCallback.onLoaded();
                }
            };

            // Send request to server...
            new Dates().updateDates(datesCallback);
        } else {
            HolidayHolder.load(context, () -> {
                // If the dates are already loaded stop process...
                if (events == null) {
                    readSavedDates(context);
                    events.addAll(HolidayHolder.getHolidays());
                    sortEvents(context);
                    createCalendar(context);
                }
                if (datesLoadedCallback != null) datesLoadedCallback.onLoaded();
            });
        }
    }

    public static boolean isLoaded() {
        return events != null && calendar != null && HolidayHolder.getHolidays().size() > 0;
    }

    public static void setCategories(Context context, List<Category> categories) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String prefString = "";

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            String categoryString = String.format(Locale.GERMAN, "%s::%s::%d:%d:%d", category.name, category.isSchool ? "true" : "false", category.color.r, category.color.g, category.color.b);
            prefString = String.format("%s#%s", prefString, categoryString);
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("saved_categories", prefString);
        editor.apply();

        DatesHolder.categories = categories;
    }

    private static void loadCategories(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedCategories = sharedPref.getString("saved_categories", context.getString(R.string.default_categories));

        categories = new ArrayList<>();

        String[] fragments = savedCategories.split("#");
        for (int i = 1; i < fragments.length; i++) {
            // Example string: holidays::false::0:0:255
            Color color = new Color(Integer.parseInt(fragments[i].split("::")[2].split(":")[0]), Integer.parseInt(fragments[i].split("::")[2].split(":")[1]), Integer.parseInt(fragments[i].split("::")[2].split(":")[2]));
            categories.add(new Category(fragments[i].split("::")[0], color, fragments[i].split("::")[1].equals("true")));
        }

        // TODO: This line is only for old version to add the category first time in the preferences... (Later this line can be deleted!)
        if (getCategory(context.getString(R.string.holiday_category)) == null)
            categories.add(new Category(context.getString(R.string.holiday_category), new Color(96, 73, 43), false));
    }

    public static List<Category> getCategories() {
        return categories;
    }

    public static Category getCategory(String name) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).name.equals(name)) return categories.get(i);
        }

        return null;
    }

    public static Category getCategory(int index) {
        return categories.get(index);
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

            events = new ArrayList<>();

            Category holidays = getCategory(context.getString(R.string.holidays_category));
            Category other = getCategory(context.getString(R.string.other_category));

            // Convert open door day...
            Date date = new Date(openDoorDayO.getInt("day"), openDoorDayO.getString("month"), openDoorDayO.getInt("year"));
            events.add(new Event(openDoorDayO.getString("description"), date, date, other));

            // Convert holidays...
            for (int i = 0; i < holidaysO.length(); i++) {
                JSONObject holdiayObject = holidaysO.getJSONObject(i);
                Date start = new Date(holdiayObject.getJSONObject("start").getString("weekday"), holdiayObject.getJSONObject("start").getInt("day"), holdiayObject.getJSONObject("start").getString("month"), holdiayObject.getJSONObject("start").getInt("year"));
                if (holdiayObject.getJSONObject("end").has("weekday")) {
                    Date end = new Date(holdiayObject.getJSONObject("end").getString("weekday"), holdiayObject.getJSONObject("end").getInt("day"), holdiayObject.getJSONObject("end").getString("month"), holdiayObject.getJSONObject("end").getInt("year"));
                    events.add(new Event(holdiayObject.getString("name"), "holidays", start, end, holidays));
                } else {
                    events.add(new Event(holdiayObject.getString("name"), "holidays", start, start, holidays));
                }
            }

            // Convert free days...
            for (int i = 0; i < freeDaysO.length(); i++) {
                JSONObject freeDateObject = freeDaysO.getJSONObject(i);
                date = new Date(freeDateObject.getString("weekday"), freeDateObject.getInt("day"), freeDateObject.getString("month"), freeDateObject.getInt("year"));
                events.add(new Event(freeDateObject.getString("description"), "freeDate", date, date, other));
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
                events.add(new Event(consultationDayObject.getString("description"), "consultationDay", start, end, other));
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
                events.add(new Event(conferenceObject.getString("description"), "conference", start, start, other));
            }

            // Convert grades releases...
            for (int i = 0; i < gradesReleasesO.length(); i++) {
                JSONObject gradesReleaseObject = gradesReleasesO.getJSONObject(i);
                JSONObject day = gradesReleaseObject.getJSONObject("day");
                Date start = new Date(day.getString("weekday"), day.getInt("day"), day.getString("month"), day.getInt("year"));
                Date end = new Date(day.getString("weekday"), day.getInt("day"), day.getString("month"), day.getInt("year"));
                start.setTime(0, 8);
                end.setTime(0, Integer.parseInt(gradesReleaseObject.getString("schoolOff").split(" ")[1]));
                events.add(new Event(gradesReleaseObject.getString("description"), "gradeReleas", start, end, other));
            }

            loadCustomEvents(context);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VsaApp/DatesHolder", "Cannot convert JSONarray!");
        }
    }

    private static void loadCustomEvents(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String[] events = sharedPref.getString("custom_events", "").split("#");

        // Event format: NAME:INFO:CATEGORY:MINUTE.HOUR.DAY.MONTH.YEAR:MINUTE.HOUR.DAY.MONTH.YEAR

        customEvents = new ArrayList<>();

        for (int i = 1; i < events.length; i++) {
            String[] prefs = events[i].split(":");
            String[] dateStart = prefs[3].split("\\.");
            String[] dateEnd = prefs[4].split("\\.");
            Event event = new Event(prefs[0], new Date(Integer.parseInt(dateStart[0]), Integer.parseInt(dateStart[1]), Integer.parseInt(dateStart[2]), Integer.parseInt(dateStart[3]), Integer.parseInt(dateStart[4])), new Date(Integer.parseInt(dateEnd[0]), Integer.parseInt(dateEnd[1]), Integer.parseInt(dateEnd[2]), Integer.parseInt(dateStart[3]), Integer.parseInt(dateStart[4])), getCategory(prefs[2]));
            customEvents.add(event);
        }

        DatesHolder.events.addAll(customEvents);
    }

    public static boolean isCustomEvent(Event event) {
        return customEvents.contains(event);
    }

    public static boolean updateEvent(Context context, Event event, Event event2) {
        String eventString = String.format(Locale.GERMAN, "%s:%s:%s:%d.%d.%d.%d.%d:%d.%d.%d.%d.%d", event.name, event.info, event.category.name, event.start.getMin(), event.start.getHour(), event.start.getDay(), event.start.getMonth(context), event.start.getYear(), event.end.getMin(), event.end.getHour(), event.end.getDay(), event.end.getMonth(context), event.end.getYear());
        String newEventString = String.format(Locale.GERMAN, "%s:%s:%s:%d.%d.%d.%d.%d:%d.%d.%d.%d.%d", event2.name, event2.info, event2.category.name, event2.start.getMin(), event2.start.getHour(), event2.start.getDay(), event2.start.getMonth(context), event2.start.getYear(), event2.end.getMin(), event.start.getHour(), event2.end.getDay(), event2.end.getMonth(context), event2.end.getYear());

        if (eventString.equals(newEventString)) return false;

        event.name = event2.name;
        event.start = event2.start;
        event.end = event2.end;
        event.info = event2.info;
        event.category = event2.category;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String[] events = sharedPref.getString("custom_events", "").split("#");

        String newPref = "";
        for (int i = 1; i < events.length; i++) {
            if (!events[i].equals(eventString))
                newPref = String.format("%s#%s", newPref, events[i]);
            else newPref = String.format("%s#%s", newPref, newEventString);
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("custom_events", newPref);
        editor.apply();
        createCalendar(context);

        return true;
    }

    public static void addEvent(Context context, Event event) {
        customEvents.add(event);
        events.add(event);
        sortEvents(context);
        createCalendar(context);

        // Event format: NAME:INFO:CATEGORY:MINUTE.HOUR.DAY.MONTH.YEAR:MINUTE.HOUR.DAY.MONTH.YEAR

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("custom_events", String.format(Locale.GERMAN, "%s#%s:%s:%s:%d.%d.%d.%d.%d:%d.%d.%d.%d.%d", sharedPref.getString("custom_events", ""), event.name, event.info, event.category.name, event.start.getMin(), event.start.getHour(), event.start.getDay(), event.start.getMonth(context), event.start.getYear(), event.end.getMin(), event.end.getHour(), event.end.getDay(), event.end.getMonth(context), event.end.getYear()));
        editor.apply();
    }

    public static void delEvent(Context context, Event event) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String[] events = sharedPref.getString("custom_events", "").split("#");
        String eventString = String.format(Locale.GERMAN, "%s:%s:%s:%d.%d.%d.%d.%d:%d.%d.%d.%d.%d", event.name, event.info, event.category.name, event.start.getMin(), event.start.getHour(), event.start.getDay(), event.start.getMonth(context), event.start.getYear(), event.end.getMin(), event.end.getHour(), event.end.getDay(), event.end.getMonth(context), event.end.getYear());

        String newPref = "";
        for (int i = 1; i < events.length; i++) {
            if (!events[i].equals(eventString))
                newPref = String.format("%s#%s", newPref, events[i]);
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("custom_events", newPref);
        editor.apply();
        DatesHolder.events.remove(event);
        customEvents.remove(event);
        createCalendar(context);
    }

    private static void sortEvents(Context c) {
        List<Event> sortedList = new ArrayList<>();
        sortedList.add(getEvents().get(0));
        for (int i = 1; i < getEvents().size(); i++) {
            Date date = getEvents().get(i).start;
            int j;
            for (j = 0; j < sortedList.size(); j++) {
                if (date.getTimestamp(c) <= sortedList.get(j).start.getTimestamp(c)) break;
            }
            sortedList.add(j, getEvents().get(i));
        }
        events = sortedList;
    }

    private static void createCalendar(Context c) {
        calendar = new ArrayList<>();


        for (int i = 0; i < getEvents().size(); i++) {
            Event event = getEvents().get(i);
            if (calendar.size() > 0) {
                if (event.start.getYear() == calendar.get(calendar.size() - 1).year) {
                    if (event.start.getMonth(c) == calendar.get(calendar.size() - 1).month) {
                        if (event.start.getDay() == calendar.get(calendar.size() - 1).day) {
                            calendar.get(calendar.size() - 1).addEvent(event);
                            continue;
                        }
                    }
                }
            }
            Day day = new Day(event.start.getDay(), event.start.getMonth(c), event.start.getYear());
            day.addEvent(event);
            calendar.add(day);
        }
    }

    private static List<Event> getEvents() {
        return events;
    }

    public static List<Day> getFilteredCalendar(Context c) {
        List<Day> filteredCalendar = getCalendar();
        java.util.Date date = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Date today = new Date(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));

        for (int i = filteredCalendar.size() - 1; i >= 0; i--) {
            if (filteredCalendar.get(i).getEvent(0).end.getTimestamp(c) < today.getTimestamp(c))
                filteredCalendar.remove(i);
        }

        return filteredCalendar;
    }

    public static List<Day> getCalendar() {
        return calendar;
    }

    public static Day getDay(Context context, int year, int month, int day) {
        return getMonth(context, month, year).get(day - 1);
    }

    public static List<Day> getMonth(Context c, int month, int year) {
        List<Day> monthList = new ArrayList<Day>();

        Calendar cl = Calendar.getInstance();
        cl.set(Calendar.YEAR, year);
        cl.set(Calendar.MONTH, month);
        int numDays = cl.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < numDays; i++) monthList.add(new Day(i + 1, month, year));

        month++;

        for (int i = 0; i < calendar.size(); i++) {
            Day day = calendar.get(i);
            for (int j = 0; j < day.getEvents().size(); j++) {
                Event event = day.getEvent(j);
                if (event.start.getYear() <= year && event.end.getYear() >= year) {
                    int firstMonthInYear = (event.start.getYear() < year) ? 1 - event.start.getMonth(c) : event.start.getMonth(c);
                    int lastMonthInYear = (event.end.getYear() > year) ? 12 + event.end.getMonth(c) : event.end.getMonth(c);
                    if (firstMonthInYear <= month && lastMonthInYear >= month) {
                        int firstDayInMonth = (firstMonthInYear < month) ? 0 : event.start.getDay() - 1;
                        int lastDayInMonth = (lastMonthInYear > month) ? numDays - 1 : event.end.getDay() - 1;
                        for (int k = firstDayInMonth; k <= lastDayInMonth; k++) {
                            monthList.get(k).addEvent(event);
                        }
                    }
                }
            }
        }

        return monthList;
    }
}
