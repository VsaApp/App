package de.lohl1kohl.vsaapp.fragments.teachers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.lohl1kohl.vsaapp.Callbacks;
import de.lohl1kohl.vsaapp.Callbacks.teachersCallback;
import de.lohl1kohl.vsaapp.SubjectSymbolsHolder;

public class TeacherHolder {
    @SuppressLint("StaticFieldLeak")
    private static List<Teacher> teachers = new ArrayList<>();

    public static void load(Context context, boolean update) {
        load(context, update, null);
    }

    public static void load(Context context, boolean update, Callbacks.teachersLoadedCallback teachersLoadedCallback) {
        List<Teacher> savedTeachers = getSavedTeachers(context);
        if (savedTeachers != null) {
            teachers = savedTeachers;
        } else {
            update = true;
        }
        if (update) {
            teachersCallback teachersCallback = new teachersCallback() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onReceived(String output) {
                    TeacherHolder.teachers = convertJsonToArray(context, output);
                    if (teachersLoadedCallback != null) teachersLoadedCallback.onNewLoaded();
                }

                @Override
                public void onConnectionFailed() {
                    if (teachersLoadedCallback != null) teachersLoadedCallback.onConnectionFailed();
                }
            };
            new Teachers().updateSp(teachersCallback);
        }
    }

    public static List<Teacher> searchTeachers(String str) {
        str = str.toLowerCase();
        List<Teacher> teachers = new ArrayList<>();
        for (Teacher teacher : TeacherHolder.teachers) {
            if (teacher.getShortName().toLowerCase().contains(str)) {
                teachers.add(teacher);
            } else if (teacher.getLongName().toLowerCase().contains(str)) {
                teachers.add(teacher);
            } else {
                for (String subject : teacher.getSubjects()) {
                    if (SubjectSymbolsHolder.get(subject).toLowerCase().contains(str)) {
                        teachers.add(teacher);
                        break;
                    }
                }
            }
        }
        return teachers;
    }

    public static Teacher searchTeacher(String str) {
        str = str.toLowerCase();
        List<Teacher> teachers = new ArrayList<>();
        for (Teacher teacher : TeacherHolder.teachers) {
            if (teacher.getShortName().toLowerCase().contains(str)) {
                return teacher;
            } else if (teacher.getLongName().toLowerCase().contains(str)) {
                return teacher;
            }
        }
        return null;
    }

    private static List<Teacher> convertJsonToArray(Context context, String array) {
        List<Teacher> teachers = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(array);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                List<String> subjects = new ArrayList<>();
                for (int j = 0; j < jsonObject.getJSONArray("subjects").length(); j++) {
                    subjects.add(jsonObject.getJSONArray("subjects").getString(j));
                }
                teachers.add(new Teacher(context, jsonObject.getString("shortName"), jsonObject.getString("longName"), jsonObject.getString("gender"), subjects.toArray(new String[0])));
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("pref_teachers", array);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return teachers;
    }

    private static List<Teacher> getSavedTeachers(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedTeachers = sharedPref.getString("pref_teachers", "-1");

        if (savedTeachers.equals("-1")) return null;
        return convertJsonToArray(context, savedTeachers);
    }

    public static List<Teacher> getTeachers() {
        return teachers;
    }
}
