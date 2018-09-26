package de.lohl1kohl.vsaapp.holders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.sp.Subject;
import de.lohl1kohl.vsaapp.fragments.vp.Today;
import de.lohl1kohl.vsaapp.fragments.vp.Tomorrow;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class VpHolder {
    public static String weekdayToday, dateToday, timeToday;
    public static String weekdayTomorrow, dateTomorrow, timeTomorrow;
    private static List<List<Subject>> vp;
    private static int countDownloadedVps = 0;

    public static void load(Context context, boolean update) {
        load(context, update, null);
    }

    public static void load(Context context, boolean update, Callbacks.baseLoadedCallback vpLoadedCallback) {
        // Get grade...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");

        vp = new ArrayList<>();
        countDownloadedVps = 0;

        for (int i = 0; i < 2; i++) {
            boolean today = (i == 0);

            if (update) {
                // Create callback...
                Callbacks.baseCallback callback = new Callbacks.baseCallback() {
                    @Override
                    public void onReceived(String output) {
                        List<Subject> newVp = convertJsonToArray(context, output, today);
                        vp.add(today ? 0 : (vp.size() == 0 ? 0 : 1), (newVp != null) ? newVp : new ArrayList<>());

                        if (newVp != null) {
                            // Save the current sp...
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("pref_vp_" + grade + "_" + (today ? "today" : "tomorrow"), output);
                            editor.apply();

                            if (vpLoadedCallback != null && countDownloadedVps == 2)
                                vpLoadedCallback.onLoaded();
                        }
                        else {
                            Log.e("Vsa/Vp", "Size of vp == 0");
                            Toast.makeText(context, String.format(context.getString(R.string.convertingFailed), "VP"), Toast.LENGTH_SHORT).show();
                            if (vpLoadedCallback != null && countDownloadedVps == 2)
                                vpLoadedCallback.onLoaded();
                        }

                        countDownloadedVps++;
                    }

                    @Override
                    public void onConnectionFailed() {
                        // Show saved sp...
                        List<Subject> savedVP = getSavedVp(context, today);
                        if (savedVP != null) vp.add(today ? 0 : 1, savedVP);

                        countDownloadedVps++;

                        if (vpLoadedCallback != null && countDownloadedVps == 2)
                            vpLoadedCallback.onLoaded();
                    }
                };
                new Thread(() -> {
                    // Send request to server...
                    if (today) {
                        new Today().updateVp(grade, callback);
                    } else {
                        new Tomorrow().updateVp(grade, callback);
                    }
                }).start();
            } else {
                // Show saved sp first...
                List<Subject> savedVP = getSavedVp(context, today);
                if (savedVP != null) vp.add(today ? 0 : 1, savedVP);
                countDownloadedVps++;
                if (vpLoadedCallback != null && countDownloadedVps == 2)
                    vpLoadedCallback.onLoaded();
            }
        }
    }

    private static boolean isShowOnlySelectedSubjects(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean("pref_showVpOnlyForYou", true);
    }

    public static void updateVpList(Context context) {
        if (isShowOnlySelectedSubjects(context)) {
            vp.clear();
            vp.add(getSavedVp(context, true));
            vp.add(getSavedVp(context, false));
        }
    }

    @Nullable
    private static List<Subject> getSavedVp(Context context, boolean today) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");
        String savedVP = sharedPref.getString("pref_vp_" + grade + "_" + (today ? "today" : "tomorrow"), "-1");

        if (savedVP.equals("-1")) return null;
        List<Subject> vp = convertJsonToArray(context, savedVP, today);
        return (vp != null) ? vp : new ArrayList<>();
    }

    @Nullable
    private static List<Subject> convertJsonToArray(Context context, String array, boolean today) {
        List<Subject> subjects = new ArrayList<>();
        try {
            JSONObject header = new JSONObject(array);
            String date = header.getString("date");
            String weekday = header.getString("weekday");
            String time = header.getString("time");
            JSONArray jsonarray = new JSONArray(header.getString("changes"));
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject entry = jsonarray.getJSONObject(i);
                int unit = Integer.valueOf(entry.getString("unit")) - 1;
                String normalLesson = entry.getString("lesson");
                JSONObject changed = new JSONObject(entry.getString("changed"));

                String info = changed.getString("info");
                String teacher = changed.getString("teacher");
                String room = changed.getString("room");

                if (SubjectSymbolsHolder.has(info.split(" ")[0].toUpperCase())) {
                    info = info.replace(info.split(" ")[0], SubjectSymbolsHolder.get(info.split(" ")[0].toUpperCase()));
                }
                try {
                    Subject subject = SpHolder.getSubject(context, weekday, unit, normalLesson.split(" ")[0]);
                    if (subject == null)
                        subject = new Subject(weekday, unit, normalLesson, "?", "");
                    subject.changes = new Subject(weekday, unit, info, room, teacher);
                    if (!isShowOnlySelectedSubjects(context) || compaireSubjects(context, subject, SpHolder.getLesson(Arrays.asList(context.getResources().getStringArray(R.array.weekdays)).indexOf(weekday), unit).getSubject())) {
                        subjects.add(subject);
                    }
                } catch (IndexOutOfBoundsException ignored) {

                }

            }

            if (today) {
                weekdayToday = weekday;
                dateToday = date;
                timeToday = time;
            } else {
                weekdayTomorrow = weekday;
                dateTomorrow = date;
                timeTomorrow = time;
            }

        } catch (JSONException e) {
            Log.e("VsaApp/VpHolder", "Cannot convert output to array!");
            return null;
        }

        return subjects;
    }

    private static boolean compaireSubjects(Context context, Subject s1, Subject s2){
        if (s1 == s2) return true;
        return s2.getName().equals(context.getString(R.string.lesson_tandem)) && (s1.getName().equals(context.getString(R.string.lesson_french)) || s1.getName().equals(context.getString(R.string.lesson_latin)));
    }

    public static List<List<Subject>> getVp() {
        return vp;
    }

    public static List<Subject> getVp(boolean today) {
        return vp.get(today ? 0 : 1);
    }

    public static Subject getSubject(boolean today, int i) {
        return vp.get(today ? 0 : 1).get(i);
    }
}
