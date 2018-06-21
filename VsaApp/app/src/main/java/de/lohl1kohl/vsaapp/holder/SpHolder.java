package de.lohl1kohl.vsaapp.holder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.lohl1kohl.vsaapp.Lesson;
import de.lohl1kohl.vsaapp.LessonUtils;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.Subject;
import de.lohl1kohl.vsaapp.holder.Callbacks.spLoadedCallback;
import de.lohl1kohl.vsaapp.server.Callbacks.spCallback;
import de.lohl1kohl.vsaapp.server.Sp;

public class SpHolder {
    @SuppressLint("StaticFieldLeak")
    public static Activity mActivity;
    public static Map<String, String> subjectsSymbols;
    private static List<List<Lesson>> sp;
    private static String lastGrade = "";

    public static void load() {
        load(null);
    }

    public static void load(spLoadedCallback spLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");

        if (grade.equals(lastGrade)) {
            if (spLoadedCallback != null) spLoadedCallback.onFinished();
            return;
        }
        lastGrade = grade;

        // Show the old sp first (for a faster reaction time)...
        sp = getSavedSp();
        if (spLoadedCallback != null) spLoadedCallback.onFinished();

        spCallback spCallback = new spCallback() {

            public void onReceived(String output) {
                sp = convertJsonToArray(output);

                // Save the current sp in the settings...
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pref_sp_" + grade, output);
                editor.apply();

                if (spLoadedCallback != null) spLoadedCallback.onFinished();
            }

            public void onConnectionFailed() {
                Log.e("VsaApp/SpHolder", "No connection!");
                sp = getSavedSp();
                if (spLoadedCallback != null) spLoadedCallback.onConnectionFailed();
            }

            public void onNoSp() {
                Log.i("VsaApp/SpHolder", "No sp!");

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

    @Nullable
    private static List<List<Lesson>> getSavedSp() {
        if (mActivity == null) return null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");
        String savedSP = sharedPref.getString("pref_sp_" + grade, "-1");

        return convertJsonToArray(savedSP);
    }

    @Nullable
    private static List<List<Lesson>> convertJsonToArray(String array) {
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
                        ls.readSavedSubject(mActivity);
                    }
                    try {
                        boolean isPassed = LessonUtils.isLessonPassed(ls.getSubject().unit);
                        boolean isFuture = LessonUtils.isDayInFuture(ls.getSubject().day);
                        //Log.i("VsaApp", "Day: " + ls.getSubject().day + ", Unit: " + ls.getSubject().unit + ", isPassed: " + (isPassed ? "true" : "false") + ", isFuture: " + (isFuture ? "true" : "false"));
                        if (LessonUtils.isDayPassed(ls.getSubject().day)) ls.setGray(true);
                        else if (isPassed && !isFuture) {
                            ls.setGray(true);
                        }
                    } catch (Exception ignored) {

                    }
                    spDay.add(ls);
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

    public static Lesson getLesson(int day, int unit) {
        return sp.get(day).get(unit);
    }

    public static Subject getSubject(String weekday, int unit, String normalSubject) {
        int day = Arrays.asList(mActivity.getResources().getStringArray(R.array.weekdays)).indexOf(weekday);
        Lesson lesson = sp.get(day).get(unit);

        for (int i = 0; i < lesson.numberOfSubjects(); i++) {
            if (lesson.getSubject(i).name.toLowerCase().equals(normalSubject)) {
                return lesson.getSubject(i);
            }
        }

        return null;
    }
}
