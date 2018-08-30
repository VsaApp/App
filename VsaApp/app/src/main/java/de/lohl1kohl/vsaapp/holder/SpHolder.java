package de.lohl1kohl.vsaapp.holder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.lohl1kohl.vsaapp.Lesson;
import de.lohl1kohl.vsaapp.LessonUtils;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.Subject;
import de.lohl1kohl.vsaapp.holder.Callbacks.spLoadedCallback;
import de.lohl1kohl.vsaapp.server.Callbacks.spCallback;
import de.lohl1kohl.vsaapp.server.Sp;

public class SpHolder {

    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;

    private static List<List<Lesson>> sp;
    private static String lastGrade = "";

    public static void load(Context context, boolean update) {
        load(context, update, null);
    }

    public static void load(Context context, boolean update, spLoadedCallback spLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");

        if (grade.equals(lastGrade)) {
            if (spLoadedCallback != null) spLoadedCallback.onOldLoaded();
            return;
        }
        lastGrade = grade;

        // Show the old sp first (for a faster reaction time)...
        sp = getSavedSp(context);
        if (spLoadedCallback != null) spLoadedCallback.onOldLoaded();

        if (update) {

            spCallback spCallback = new spCallback() {

                public void onReceived(String output) {
                    sp = convertJsonToArray(context, output);

                    // Save the current sp in the settings...
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("pref_sp_" + grade, output);
                    editor.apply();

                    if (spLoadedCallback != null) spLoadedCallback.onNewLoaded();
                }

                public void onConnectionFailed() {
                    sp = getSavedSp(context);
                    if (spLoadedCallback != null) spLoadedCallback.onConnectionFailed();
                }

                public void onNoSp() {
                    // Create a empty sp...
                    sp = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        sp.add(new ArrayList<>());
                    }
                    if (spLoadedCallback != null) spLoadedCallback.onNoSp();
                }
            };

            // Send request to server...
            new Sp().updateSp(grade, spCallback);
        }
    }

    @Nullable
    private static List<List<Lesson>> getSavedSp(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");
        String savedSP = sharedPref.getString("pref_sp_" + grade, "-1");

        if (savedSP.equals("-1")) {
            return new ArrayList<>();
        }
        return convertJsonToArray(context, savedSP);
    }

    @Nullable
    private static List<List<Lesson>> convertJsonToArray(Context context, String array) {
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
            return null;
        }

        return sp;
    }

    public static List<List<Lesson>> getSp() {
        return sp;
    }

    public static List<Lesson> getDay(int day) {
        return sp.get(day);
    }

    public static int getNumberOfLessons(int weekday) {
        return sp.get(weekday).size();
    }

    public static Lesson getLesson(int day, int unit) {
        return sp.get(day).get(unit);
    }

    public static Subject getSubject(Context context, String weekday, int unit, String normalSubject) {
        int day = Arrays.asList(context.getResources().getStringArray(R.array.weekdays)).indexOf(weekday);
        Lesson lesson = sp.get(day).get(unit);

        for (int i = 0; i < lesson.numberOfSubjects(); i++) {
            if (lesson.getSubject(i).name.toLowerCase().equals(normalSubject.toLowerCase())) {
                return lesson.getSubject(i);
            }
        }

        return null;
    }
}
