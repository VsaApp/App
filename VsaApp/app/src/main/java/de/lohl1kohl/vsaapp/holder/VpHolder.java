package de.lohl1kohl.vsaapp.holder;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import java.util.Map;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.Subject;
import de.lohl1kohl.vsaapp.holder.Callbacks.vpLoadedCallback;
import de.lohl1kohl.vsaapp.server.Callbacks.vpCallback;
import de.lohl1kohl.vsaapp.server.vp.Today;
import de.lohl1kohl.vsaapp.server.vp.Tomorrow;

public class VpHolder {
    @SuppressLint("StaticFieldLeak")
    public static Activity mActivity;
    public static Map<String, String> subjectsSymbols;
    public static String weekdayToday, dateToday, timeToday;
    public static String weekdayTomorrow, dateTomorrow, timeTomorrow;
    private static List<List<Subject>> vp;
    private static int countDownloadedVps = 0;

    public static void load(vpLoadedCallback vpLoadedCallback) {
        // Get grade...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");

        vp = new ArrayList<>();
        countDownloadedVps = 0;

        for (int i = 0; i < 2; i++) {
            boolean today = (i == 0);

            // Show saved sp first...
            List<Subject> savedVP = getSavedVp(today);
            if (savedVP != null) vp.add(today ? 0 : 1, savedVP);
            if (vpLoadedCallback != null && countDownloadedVps == 2) vpLoadedCallback.onFinished();

            // Create callback...
            vpCallback callback = new vpCallback() {
                @Override
                public void onReceived(String output) {
                    vp.add(today ? 0 : 1, convertJsonToArray(output, today));
                    Log.v("VsaApp/Server", "Success");

                    // Save the current sp...
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("pref_vp_" + grade + "_" + (today ? "today" : "tomorrow"), output);
                    editor.apply();

                    countDownloadedVps++;

                    if (vpLoadedCallback != null && countDownloadedVps == 2)
                        vpLoadedCallback.onFinished();
                }

                @Override
                public void onConnectionFailed() {
                    Log.e("VsaApp/Server", "Failed");
                    Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();

                    // Show saved sp...
                    List<Subject> savedVP = getSavedVp(today);
                    if (savedVP != null) vp.add(today ? 0 : 1, savedVP);

                    if (vpLoadedCallback != null && countDownloadedVps == 2)
                        vpLoadedCallback.onConnectionFailed();
                }
            };

            // Send request to server...
            if (today) {
                new Today().updateVp(grade, callback);
            } else {
                new Tomorrow().updateVp(grade, callback);
            }
        }
    }

    @Nullable
    private static List<Subject> getSavedVp(boolean today) {
        if (mActivity == null) return null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");
        String savedVP = sharedPref.getString("pref_vp_" + grade + "_" + (today ? "today" : "tomorrow"), "-1");

        if (savedVP.equals("-1")) return null;
        return convertJsonToArray(savedVP, today);
    }

    private static List<Subject> convertJsonToArray(String array, boolean today) {
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
                String tutor = changed.getString("teacher");
                String room = changed.getString("room");

                if (subjectsSymbols.containsKey(info.split(" ")[0].toUpperCase())) {
                    info = info.replace(info.split(" ")[0], subjectsSymbols.get(info.split(" ")[0].toUpperCase()));
                }

                Subject subject = SpHolder.getSubject(weekday, unit, normalLesson);
                if (subject == null)
                    subject = SpHolder.getLesson(Arrays.asList(mActivity.getResources().getStringArray(R.array.weekdays)).indexOf(weekday), unit).getSubject();
                subject.changes = new Subject(weekday, unit, info, room, tutor);

                subjects.add(subject);
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
            Log.i("VsaApp/SpFragment", "Cannot convert output to array!");
        }

        return subjects;
    }

    public static List<List<Subject>> getVp() {
        return vp;
    }

    public static List<Subject> getVp(boolean today) {
        return vp.get(today ? 0 : 1);
    }

    public static Subject getSubject(boolean today, int i) {
        Log.i("VsaApp/VpHolder", "Today: " + (today ? "true" : "false") + ", i:" + i + ", " + vp);
        return vp.get(today ? 0 : 1).get(i);
    }
}
