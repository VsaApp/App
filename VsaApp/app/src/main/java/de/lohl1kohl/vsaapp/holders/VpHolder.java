package de.lohl1kohl.vsaapp.holders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.sp.Lesson;
import de.lohl1kohl.vsaapp.fragments.sp.Subject;
import de.lohl1kohl.vsaapp.fragments.vp.Today;
import de.lohl1kohl.vsaapp.fragments.vp.Tomorrow;
import de.lohl1kohl.vsaapp.loader.Callbacks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class VpHolder {
    public static String weekdayToday, dateToday, timeToday, updateToday;
    public static String weekdayTomorrow, dateTomorrow, timeTomorrow, updateTomorrow;
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
        List<String> outputs = new ArrayList<>();
        outputs.add("");
        outputs.add("");

        for (int i = 0; i < 2; i++) {
            boolean today = (i == 0);

            if (update) {
                // Create callback...
                Callbacks.baseCallback callback = new Callbacks.baseCallback() {
                    @Override
                    public void onReceived(String output) {
                        outputs.set(today ? 0 : 1, output);

                        countDownloadedVps++;

                        if (countDownloadedVps == 2) {
                            Calendar day1 = getDate(outputs.get(0));
                            Calendar day2 = getDate(outputs.get(1));

                            boolean today = true;
                            do {
                                List<Subject> subjects;
                                subjects = convertJsonToArray(context, outputs.get(today ? 0 : 1), today, today && day1.get(Calendar.DAY_OF_MONTH) == day2.get(Calendar.DAY_OF_MONTH));
                                vp.add(today ? 0 : (vp.size() == 0 ? 0 : 1), (subjects != null) ? subjects : new ArrayList<>());
                                if (subjects != null) {
                                    // Save the current sp...
                                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString("pref_vp_" + grade + "_" + (today ? "today" : "tomorrow"), outputs.get(today ? 0 : 1));
                                    editor.apply();

                                    if (vpLoadedCallback != null)
                                        vpLoadedCallback.onLoaded();
                                } else {
                                    Log.e("Vsa/Vp", "Size of vp == 0");
                                    Toast.makeText(context, String.format(context.getString(R.string.convertingFailed), "VP"), Toast.LENGTH_SHORT).show();
                                    if (vpLoadedCallback != null)
                                        vpLoadedCallback.onLoaded();
                                }
                                today = !today;
                            } while (!today);

                            if (day1.get(Calendar.DAY_OF_MONTH) == day2.get(Calendar.DAY_OF_MONTH))
                                vp.set(0, vp.get(1));
                        }
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

    private static Calendar getDate(String array) {
        try {
            JSONObject header = new JSONObject(array);
            String date = header.getString("date");

            Calendar day = Calendar.getInstance();
            day.setTime(new Date());
            day.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.split("\\.")[0]));
            day.set(Calendar.MONTH, Integer.parseInt(date.split("\\.")[1]));
            day.set(Calendar.YEAR, Integer.parseInt(date.split("\\.")[2]));
            return day;

        } catch (JSONException e) {
            Log.e("VsaApp/VpHolder", "Cannot convert output to array!");
            return null;
        }
    }

    public static boolean isLoaded() {
        return vp.size() > 0;
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
        boolean onlyHeader = false;

        if (today) {
            String savedVPTomorrow = sharedPref.getString("pref_vp_" + grade + "_tomorrow", "-1");
            Calendar day1 = getDate(savedVP);
            Calendar day2 = getDate(savedVPTomorrow);
            if (day1.get(Calendar.DAY_OF_MONTH) == day2.get(Calendar.DAY_OF_MONTH)) onlyHeader = true;
        }

        if (savedVP.equals("-1")) return null;
        List<Subject> vp = convertJsonToArray(context, savedVP, today, onlyHeader);
        return (vp != null) ? vp : new ArrayList<>();
    }

    @Nullable
    private static List<Subject> convertJsonToArray(Context context, String array, boolean today, boolean onlyHeader) {
        List<Subject> subjects = new ArrayList<>();
        try {
            JSONObject header = new JSONObject(array);
            String date = header.getString("date");
            String weekday = header.getString("weekday");
            String time = header.getString("time");
            String update = header.getString("update");
            if (!onlyHeader) {
                JSONArray jsonarray = new JSONArray(header.getString("changes"));
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject entry = jsonarray.getJSONObject(i);
                    int unit = Integer.valueOf(entry.getString("unit")) - 1;
                    String normalLesson = entry.getString("lesson");
                    String normalTeacher = entry.getString("teacher");
                    JSONObject changed = new JSONObject(entry.getString("changed"));

                    String info = changed.getString("info");
                    String teacher = changed.getString("teacher");
                    String room = changed.getString("room");

                    if (SubjectSymbolsHolder.has(info.split(" ")[0].toUpperCase())) {
                        info = info.replace(info.split(" ")[0], SubjectSymbolsHolder.get(info.split(" ")[0].toUpperCase()));
                    }
                    try {
                        Subject subject;
                        if (info.contains(context.getString(R.string.exam))) {
                            subject = new Subject(weekday, unit, normalLesson, "?", "");
                            subject.changes = new Subject(weekday, unit, normalLesson, room, normalTeacher);
                            try {
                                teacher = TeacherHolder.searchTeacher(teacher).getGenderizedGenitiveName();
                            } catch (Exception ignored) {

                            }
                            subject.changes.name = String.format(context.getString(R.string.exam_info), subject.changes.getName(), info, teacher);
                            if (info.equals(context.getString(R.string.exam)) && compaireSubjects(context, subject, null)) {
                                SpHolder.getDay(Arrays.asList(context.getResources().getStringArray(R.array.weekdays)).indexOf(weekday)).get(unit).getSubject().changes = new Subject(subject.changes.day, subject.changes.unit, subject.changes.name.split(" ")[0] + " " + subject.changes.name.split(" ")[1], subject.changes.room, subject.changes.teacher);
                            }
                        } else {
                            subject = SpHolder.getSubject(context, weekday, unit, normalLesson.split(" ")[0]);
                            if (subject == null)
                                subject = new Subject(weekday, unit, normalLesson, "?", "");
                            if (subject.changes == null)
                                subject.changes = new Subject(weekday, unit, info, room, teacher);
                        }
                        if (!isShowOnlySelectedSubjects(context) || compaireSubjects(context, subject, SpHolder.getLesson(Arrays.asList(context.getResources().getStringArray(R.array.weekdays)).indexOf(weekday), unit).getSubject())) {
                            subjects.add(subject);
                        }
                    } catch (IndexOutOfBoundsException ignored) {

                    }

                }
            }

            if (today) {
                weekdayToday = weekday;
                dateToday = date;
                timeToday = time;
                updateToday = update;
            } else {
                weekdayTomorrow = weekday;
                dateTomorrow = date;
                timeTomorrow = time;
                updateTomorrow = update;
            }

        } catch (JSONException e) {
            Log.e("VsaApp/VpHolder", "Cannot convert output to array!");
            return null;
        }

        return subjects;
    }

    public static boolean compaireSubjects(Context context, Subject s1, Subject s2) {
        if (s1.changes.name.contains(context.getString(R.string.make_up_exam))) return true;
        if (s1.changes.name.contains(context.getString(R.string.exam))) {
            for (int i = 0; i < 5; i++) {
                List<Lesson> day = SpHolder.getDay(i);
                for (int j = 0; j < day.size(); j++) {
                    Lesson lesson = day.get(j);
                    if (lesson.numberOfSubjects() > 0) {
                        Subject subject = lesson.getSubject();
                        if (s1.changes.teacher.equals(subject.teacher) && s1.changes.name.split(" ")[0].equals(subject.getName()))
                            return true;
                    }
                }
            }
            return false;
        }

        return s1.equals(s2) || s2.getName().equals(context.getString(R.string.lesson_tandem)) && (s1.getName().equals(context.getString(R.string.lesson_french)) || s1.getName().equals(context.getString(R.string.lesson_latin)));
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
