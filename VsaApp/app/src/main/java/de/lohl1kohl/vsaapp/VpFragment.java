package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.lohl1kohl.vsaapp.server.Callbacks;
import de.lohl1kohl.vsaapp.server.vp.Today;
import de.lohl1kohl.vsaapp.server.vp.Tomorrow;


public class VpFragment extends Fragment {
    Activity mainActivity;
    View vpView;
    private Map<String, String> subjectsSymbols = new HashMap<>();
    private List<Lesson> lessonsToday = new ArrayList<>();
    private List<Lesson> lessonsTomorrow = new ArrayList<>();
    private String weekdayToday, dateToday, timeToday;
    private String weekdayTomorrow, dateTomorrow, timeTomorrow;
    private int lessonsGot = 0;

    @SuppressLint("SetTextI18n")
    static void showVpInfoDialog(Context context, Lesson lesson) {
        final Dialog loginDialog = new Dialog(context);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(Objects.requireNonNull(loginDialog.getWindow()).getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_vp_info);
        loginDialog.setCancelable(true);
        loginDialog.setTitle(R.string.vpInfoDialogTitle);

        String tutorNormal = lesson.tutor;
        String tutorNow = lesson.changes.tutor;

        // Get long teacher name for normal lesson...
        List<String> shortNames = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.short_names)));
        List<String> longNames = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.long_names)));

        if (tutorNormal.length() > 0) {
            if (shortNames.contains(lesson.tutor)) {
                tutorNormal = longNames.get(shortNames.indexOf(tutorNormal));
                tutorNormal = tutorNormal.replace(context.getString(R.string.mister), context.getString(R.string.mister_gen));
            }
        }

        if (tutorNow.length() > 0) {
            if (shortNames.contains(lesson.tutor)) {
                tutorNow = longNames.get(shortNames.indexOf(tutorNow));
                tutorNow = tutorNow.replace(context.getString(R.string.mister), context.getString(R.string.mister_gen));
            }
        }

        // Get all TextViews...
        final TextView tV_units = loginDialog.findViewById(R.id.lbl_unit);
        final TextView tV_normal = loginDialog.findViewById(R.id.lbl_normal);
        final TextView tV_changed = loginDialog.findViewById(R.id.lbl_changed);


        tV_units.setText(Integer.toString(lesson.unit) + context.getString(R.string.dot_unit));
        tV_normal.setText(String.format(context.getString(R.string.with_s_s_in_room_s), tutorNormal, lesson.getName(), lesson.room));
        tV_normal.setPaintFlags(tV_normal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        String text;
        if (lesson.changes.tutor.length() > 0)
            text = String.format(context.getString(R.string.now_with_s_s), tutorNow, lesson.changes.name);
        else text = String.format(context.getString(R.string.now_s), lesson.changes.getName());
        if (lesson.changes.room.length() > 0)
            text += context.getString(R.string.in_room) + lesson.changes.room;
        tV_changed.setText(text);

        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vpView = inflater.inflate(R.layout.fragment_vp, container, false);
        mainActivity = getActivity();

        // Update vp...
        syncVp(true);
        syncVp(false);

        // Create dictionary with all subject symbols...
        String[] subjects = getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");

            subjectsSymbols.put(pair[0], pair[1]);
        }

        return vpView;
    }

    private void syncVp(boolean today) {
        // Get gradename...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String gradename = sharedPref.getString("pref_grade", "-1");

        // Check if a gradename is set...
        if (gradename.equals("-1")) {
            Toast.makeText(mainActivity, R.string.no_class, Toast.LENGTH_LONG).show();
            return;
        }

        // Create callback...
        Callbacks.vpCallback callback = new Callbacks.vpCallback() {
            @Override
            public void onReceived(String output) {
                fillVp(output, today);
                Log.v("VsaApp/Server", "Success");

                // Save the current sp...
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pref_vp_" + (today ? "today" : "tomorrow"), output);
                editor.apply();
            }

            @Override
            public void onConnectionFailed() {
                Log.e("VsaApp/Server", "Failed");
                Toast.makeText(mainActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();

                // Show saved sp...
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                String savedVP = sharedPref.getString("pref_vp_" + (today ? "today" : "tomorrow"), "-1");

                if (!savedVP.equals("-1")) {
                    fillVp(savedVP, today);
                }
            }
        };

        // Send request to server...
        if (today) {
            new Today().updateVp(gradename, callback);
        } else {
            new Tomorrow().updateVp(gradename, callback);
        }
    }

    private Lesson getLesson(String weekday, int unit, String normalLesson) {

        normalLesson = normalLesson.split(" ")[0].toLowerCase();

        // Get saved sp...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String savedSP = sharedPref.getString("pref_sp", "-1");

        try {
            JSONArray jsonarray = new JSONArray(savedSP);
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject day = jsonarray.getJSONObject(i);

                if (day.getString("name").equals(weekday)) {
                    JSONArray lessons = new JSONArray(day.getString("lessons"));
                    JSONArray lesson = lessons.getJSONArray(unit - 1);

                    for (int x = 0; x < lesson.length(); x++) {
                        JSONObject subject = lesson.getJSONObject(x);
                        String name = subject.getString("lesson");
                        String room = subject.getString("room");
                        String tutor = subject.getString("tutor");
                        if (name.toLowerCase().equals(normalLesson)) {
                            return new Lesson(weekday, unit, name, room, tutor, subjectsSymbols);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.i("VsaApp/SpFragment", "Cannot convert output to array!");
        }

        Log.e("VsaApp/VpFragment", "There is no lesson with the given params!");
        return new Lesson(weekday, unit, normalLesson, "?", "?", subjectsSymbols);
    }

    private void fillVp(String output, boolean today) {
        List<Lesson> lessons = new ArrayList<>();

        String weekday = null;
        String date = null;
        String time = null;

        try {
            JSONArray jsonarray = new JSONArray(output);
            lessonsGot++;
            if (jsonarray.length() == 0) {
                return;
            }
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject entry = jsonarray.getJSONObject(i);
                date = entry.getString("date");
                weekday = entry.getString("weekday");
                int unit = Integer.valueOf(entry.getString("unit"));
                time = entry.getString("time");
                String normalLesson = entry.getString("lesson");
                JSONObject changed = new JSONObject(entry.getString("changed"));

                String info = changed.getString("info");
                String tutor = changed.getString("tutor");
                String room = changed.getString("room");

                if (subjectsSymbols.containsKey(info.split(" ")[0].toUpperCase())) {
                    info = info.replace(info.split(" ")[0], subjectsSymbols.get(info.split(" ")[0].toUpperCase()));
                }

                Lesson lesson = getLesson(weekday, unit, normalLesson);
                lesson.changes = new Lesson(date, unit, info, room, tutor, subjectsSymbols);

                lessons.add(lesson);
            }

        } catch (JSONException e) {
            Log.i("VsaApp/SpFragment", "Cannot convert output to array!");
        }

        if (today) {
            lessonsToday = lessons;
            weekdayToday = weekday;
            dateToday = date;
            timeToday = time;
        } else {
            lessonsTomorrow = lessons;
            weekdayTomorrow = weekday;
            dateTomorrow = date;
            timeTomorrow = time;
        }
        if (lessonsGot == 2) {
            ViewPager pager = vpView.findViewById(R.id.vp_viewpager);
            VpDayAdapter adapter = new VpDayAdapter(getFragmentManager());
            adapter.setDataToday(lessonsToday);
            adapter.setDataTomorrow(lessonsTomorrow);
            adapter.setInfoToday(weekdayToday, dateToday, timeToday);
            adapter.setInfoTomorrow(weekdayTomorrow, dateTomorrow, timeTomorrow);
            pager.setAdapter(adapter);
        }
    }
}