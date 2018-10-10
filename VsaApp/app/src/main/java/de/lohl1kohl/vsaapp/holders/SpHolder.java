package de.lohl1kohl.vsaapp.holders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.sp.Lesson;
import de.lohl1kohl.vsaapp.fragments.sp.Sp;
import de.lohl1kohl.vsaapp.fragments.sp.Subject;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class SpHolder {

    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;

    private static List<List<Lesson>> untrimmedSp;
    private static List<List<Lesson>> sp;

    public static void load(Context context, boolean update) {
        load(context, update, null);
    }

    public static void load(Context context, boolean update, Callbacks.baseLoadedCallback baseLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");

        untrimmedSp = new ArrayList<>();

        if (update) {

            Callbacks.baseCallback spCallback = new Callbacks.baseCallback() {
                @Override
                public void onReceived(String output) {
                    sp = convertJsonToArray(context, output);

                    if (sp.size() > 0) {
                        // Save the current sp in the settings...
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("pref_sp_" + grade, output);
                        editor.apply();

                        if (baseLoadedCallback != null) baseLoadedCallback.onLoaded();
                    } else {
                        Toast.makeText(context, String.format(context.getString(R.string.convertingFailed), "SP"), Toast.LENGTH_SHORT).show();
                        if (baseLoadedCallback != null) baseLoadedCallback.onLoaded();
                    }
                    // Add lessons and trim days...
                    addSpecialLessons(context);

                    for (int i = 0; i < 5; i++) {
                        untrimmedSp.add(new ArrayList<>());
                        untrimmedSp.get(i).addAll(sp.get(i));
                    }
                    ignoreLastFreeLessons(context);
                }

                @Override
                public void onConnectionFailed() {
                    sp = getSavedSp(context);
                    // Add lessons and trim days...
                    addSpecialLessons(context);
                    for (int i = 0; i < 5; i++) {
                        untrimmedSp.add(new ArrayList<>());
                        untrimmedSp.get(i).addAll(sp.get(i));
                    }
                    ignoreLastFreeLessons(context);
                    if (baseLoadedCallback != null) baseLoadedCallback.onLoaded();
                }
            };

            // Send request to server...
            new Sp().updateSp(grade, spCallback);
        } else {
            sp = getSavedSp(context);
            // Add lessons and trim days...
            addSpecialLessons(context);
            for (int i = 0; i < 5; i++) {
                untrimmedSp.add(new ArrayList<>());
                untrimmedSp.get(i).addAll(sp.get(i));
            }
            ignoreLastFreeLessons(context);
            if (baseLoadedCallback != null) baseLoadedCallback.onLoaded();
        }
    }

    public static boolean isLoaded() {
        return sp.size() > 0;
    }

    private static void addSpecialLessons(Context context) {
        // Get preferences...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1").toUpperCase();
        List<String> weekdays = Arrays.asList(context.getResources().getStringArray(R.array.weekdays));

        for (int i = 0; i < 5; i++) {
            List<Lesson> spDay = sp.get(i);
            String weekday = weekdays.get(i);
            if (!spDay.get(0).containsSubject(context.getString(R.string.lesson_free))) {
                if (grade.equals("EF") | grade.equals("Q1") | grade.equals("Q2")) {
                    for (int unit = 0; unit < spDay.size(); unit++) {
                        Lesson lesson = spDay.get(unit);
                        if (unit == 5) continue;
                        lesson.addSubject(new Subject(weekday, unit, context.getString(R.string.lesson_free), "-", "-"));
                        lesson.readSavedSubject(context);
                    }
                }
            }
            if (!grade.equals("EF") && !grade.equals("Q1") && !grade.equals("Q2")) {
                for (int unit = 0; unit < spDay.size(); unit++) {
                    Lesson lesson = spDay.get(unit);
                    if (unit == 5) continue;
                    if (lesson.containsSubject(context.getString(R.string.lesson_tandem))) continue;
                    if (lesson.numberOfSubjects() >= 2) {
                        if (lesson.getSubject(0).getName().equals(context.getString(R.string.lesson_french)) || lesson.getSubject(0).getName().equals(context.getString(R.string.lesson_latin))) {
                            // Add the tandem lesson...
                            lesson.addSubject(new Subject(weekday, unit, context.getString(R.string.lesson_tandem), context.getString(R.string.lesson_french), context.getString(R.string.lesson_latin)));
                            lesson.readSavedSubject(context);
                        }
                    }
                    if (unit == 6 || unit == 7) {
                        lesson.addSubject(new Subject(weekday, unit, context.getString(R.string.lesson_free), "-", "-"));
                        lesson.readSavedSubject(context);
                    }
                }
            }
        }
    }

    private static void ignoreLastFreeLessons(Context context) {
        // Get preferences...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1").toUpperCase();

        for (int i = 0; i < 5; i++) {
            List<Lesson> spDay = sp.get(i);
            // Ignore the last free lessons...
            for (int j = spDay.size() - 1; j >= 0; j--) {
                if (spDay.get(j).numberOfSubjects() > 0 && !spDay.get(j).getSubject().name.equals(context.getString(R.string.lesson_free)))
                    break;
                else if (spDay.get(j).numberOfSubjects() == 0) spDay.remove(j);
                else if (spDay.get(j).getSubject().name.equals(context.getString(R.string.lesson_free)))
                    spDay.remove(j);
            }
        }
    }

    private static List<List<Lesson>> getSavedSp(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");
        String savedSP = sharedPref.getString("pref_sp_" + grade, "-1");

        if (savedSP.equals("-1")) {
            return new ArrayList<>();
        }
        return convertJsonToArray(context, savedSP);
    }

    private static List<List<Lesson>> convertJsonToArray(Context context, String array) {
        List<List<Lesson>> globalSp = sp;
        List<List<Lesson>> sp = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(array);
            for (int i = 0; i < jsonArray.length(); i++) {
                List<Lesson> spDay = new ArrayList<>();
                JSONArray l = jsonArray.getJSONObject(i).getJSONArray("lessons");
                for (int j = 0; j < l.length(); j++) {
                    JSONArray g = l.getJSONArray(j);
                    Lesson ls = new Lesson(new ArrayList<>());
                    if (g.length() > 0) {
                        if (g.length() > 1) {
                            for (int k = 0; k < g.length(); k++) {
                                Subject subject = new Subject(jsonArray.getJSONObject(i).getString("name"), j, g.getJSONObject(k).getString("lesson"), g.getJSONObject(k).getString("room"), g.getJSONObject(k).getString("teacher"));
                                ls.addSubject(subject);
                            }
                        } else {
                            Subject subject = new Subject(jsonArray.getJSONObject(i).getString("name"), j, g.getJSONObject(0).getString("lesson"), g.getJSONObject(0).getString("room"), g.getJSONObject(0).getString("teacher"));
                            ls.addSubject(subject);
                        }
                    }

                    if (ls.numberOfSubjects() > 1) {
                        ls.readSavedSubject(context);
                    }
                    spDay.add(ls);
                }
                // Delete last free lessons...
                for (int j = spDay.size() - 1; j >= 0; j--) {
                    if (spDay.get(j).numberOfSubjects() == 0) spDay.remove(j);
                    else break;
                }
                sp.add(spDay);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VsaApp/SpHolder", "Cannot convert JSONarray!");
            // Only to fix the bug in 1.1.8 (later, this can be deleted!!!)...
            if (globalSp == null) load(context, true);
            return sp;
        }

        return sp;
    }

    public static List<List<Lesson>> getSp() {
        return sp;
    }

    public static List<Lesson> getDay(int day) {
        return sp.get(day);
    }

    public static List<Lesson> getUntrimmedDay(int day) {
        return untrimmedSp.get(day);
    }

    public static int getNumberOfLessons(int weekday) {
        return sp.get(weekday).size();
    }

    public static Lesson getLesson(int day, int unit) {
        return sp.get(day).get(unit);
    }

    public static Subject getSubject(Context context, String weekday, int unit, String normalSubject) {
        int day = Arrays.asList(context.getResources().getStringArray(R.array.weekdays)).indexOf(weekday);
        Lesson lesson = getLesson(day, unit);

        for (int i = 0; i < lesson.numberOfSubjects(); i++) {
            if (lesson.getSubject(i).name.toLowerCase().equals(normalSubject.toLowerCase())) {
                return lesson.getSubject(i);
            }
        }

        return null;
    }
}
