package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
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


public class VpFragment extends BaseFragment {
    View vpView;
    String outputToday = null;
    String outputTomorrow = null;
    private Map<String, String> subjectsSymbols = new HashMap<>();

    @SuppressLint("SetTextI18n")
    static void showVpInfoDialog(Context context, Subject subject) {
        final Dialog loginDialog = new Dialog(context);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(Objects.requireNonNull(loginDialog.getWindow()).getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_vp_info);
        loginDialog.setCancelable(true);
        loginDialog.setTitle(R.string.vpInfoDialogTitle);

        String tutorNormal = subject.tutor;
        String tutorNow = subject.changes.tutor;

        // Get long teacher name for normal lesson...
        List<String> shortNames = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.short_names)));
        List<String> longNames = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.long_names)));

        if (tutorNormal.length() > 0) {
            if (shortNames.contains(subject.tutor)) {
                tutorNormal = longNames.get(shortNames.indexOf(tutorNormal));
                tutorNormal = tutorNormal.replace(context.getString(R.string.mister), context.getString(R.string.mister_gen));
            }
        }

        if (tutorNow.length() > 0) {
            if (shortNames.contains(subject.tutor)) {
                tutorNow = longNames.get(shortNames.indexOf(tutorNow));
                tutorNow = tutorNow.replace(context.getString(R.string.mister), context.getString(R.string.mister_gen));
            }
        }

        // Get all TextViews...
        final TextView tV_units = loginDialog.findViewById(R.id.lbl_unit);
        final TextView tV_normal = loginDialog.findViewById(R.id.lbl_normal);
        final TextView tV_changed = loginDialog.findViewById(R.id.lbl_changed);


        tV_units.setText(Integer.toString(subject.unit) + context.getString(R.string.dot_unit));
        tV_normal.setText(String.format(context.getString(R.string.with_s_s_in_room_s), tutorNormal, subject.getName(), subject.room));
        tV_normal.setPaintFlags(tV_normal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        String text;
        if (subject.changes.tutor.length() > 0)
            text = String.format(context.getString(R.string.now_with_s_s), tutorNow, subject.changes.name);
        else text = String.format(context.getString(R.string.now_s), subject.changes.getName());
        if (subject.changes.room.length() > 0)
            text += String.format(context.getString(R.string.in_room_s), subject.changes.room);
        tV_changed.setText(text);

        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vpView = inflater.inflate(R.layout.fragment_vp, container, false);

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String gradename = sharedPref.getString("pref_grade", "-1");

        // Check if a gradename is set...
        if (gradename.equals("-1")) {
            Toast.makeText(mActivity, R.string.no_class, Toast.LENGTH_LONG).show();
            return;
        }

        // Create callback...
        Callbacks.vpCallback callback = new Callbacks.vpCallback() {
            @Override
            public void onReceived(String output) {
                if (today) outputToday = output;
                else outputTomorrow = output;
                if (outputTomorrow != null && outputToday != null)
                    fillVp(outputToday, outputTomorrow);
                Log.v("VsaApp/Server", "Success");

                // Save the current sp...
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pref_vp_" + (today ? "today" : "tomorrow"), output);
                editor.apply();
            }

            @Override
            public void onConnectionFailed() {
                Log.e("VsaApp/Server", "Failed");
                Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();

                // Show saved sp...
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String savedVP = sharedPref.getString("pref_vp_" + (today ? "today" : "tomorrow"), "-1");

                if (!savedVP.equals("-1")) {
                    if (today) outputToday = savedVP;
                    else outputTomorrow = savedVP;
                    if (outputTomorrow != null && outputToday != null)
                        fillVp(outputToday, outputTomorrow);
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

    private Subject getSubject(String weekday, int unit, String normalSubject) {

        normalSubject = normalSubject.split(" ")[0].toLowerCase();

        // Get saved sp...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");
        String savedSP = sharedPref.getString("pref_sp_" + grade, "-1");

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
                        if (name.toLowerCase().equals(normalSubject)) {
                            return new Subject(weekday, unit, name, room, tutor, subjectsSymbols);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.i("VsaApp/SpFragment", "Cannot convert output to array!");
        }

        Log.e("VsaApp/VpFragment", "There is no lesson with the given params!");
        return new Subject(weekday, unit, normalSubject, "?", "?", subjectsSymbols);
    }

    private void fillVp(String outputToday, String outputTomorrow) {
        List<Subject> subjects;

        String weekday;
        String date;
        String time;
        List<Subject> subjectsToday = new ArrayList<>();
        List<Subject> subjectsTomorrow = new ArrayList<>();
        String weekdayToday = "", dateToday = "", timeToday = "";
        String weekdayTomorrow = "", dateTomorrow = "", timeTomorrow = "";

        for (int j = 0; j < 2; j++) {
            try {
                subjects = new ArrayList<>();
                String output = (j == 1) ? outputTomorrow : outputToday;
                JSONObject header = new JSONObject(output);
                date = header.getString("date");
                weekday = header.getString("weekday");
                time = header.getString("time");
                JSONArray jsonarray = new JSONArray(header.getString("changes"));
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject entry = jsonarray.getJSONObject(i);
                    int unit = Integer.valueOf(entry.getString("unit"));
                    String normalLesson = entry.getString("lesson");
                    JSONObject changed = new JSONObject(entry.getString("changed"));

                    String info = changed.getString("info");
                    String tutor = changed.getString("tutor");
                    String room = changed.getString("room");

                    if (subjectsSymbols.containsKey(info.split(" ")[0].toUpperCase())) {
                        info = info.replace(info.split(" ")[0], subjectsSymbols.get(info.split(" ")[0].toUpperCase()));
                    }

                    Subject lesson = getSubject(weekday, unit, normalLesson);
                    lesson.changes = new Subject(date, unit, info, room, tutor, subjectsSymbols);

                    subjects.add(lesson);
                }

            } catch (JSONException e) {
                Log.i("VsaApp/SpFragment", "Cannot convert output to array!");
                return;
            }


            if (j == 0) {
                subjectsToday = subjects;
                weekdayToday = weekday;
                dateToday = date;
                timeToday = time;
            } else {
                subjectsTomorrow = subjects;
                weekdayTomorrow = weekday;
                dateTomorrow = date;
                timeTomorrow = time;
            }
        }

        ViewPager pager = vpView.findViewById(R.id.vp_viewpager);
        VpDayAdapter adapter = new VpDayAdapter(getFragmentManager());
        adapter.setDataToday(subjectsToday);
        adapter.setDataTomorrow(subjectsTomorrow);
        adapter.setInfoToday(weekdayToday, dateToday, timeToday);
        adapter.setInfoTomorrow(weekdayTomorrow, dateTomorrow, timeTomorrow);
        pager.setAdapter(adapter);

        // Add the tabs...
        TabLayout tabLayout = vpView.findViewById(R.id.vp_tabs);
        tabLayout.setupWithViewPager(pager);

        if (mActivity.getIntent().getStringExtra("day") != null) {
            if (mActivity.getIntent().getStringExtra("day").equals(weekdayTomorrow)) {
                Objects.requireNonNull(tabLayout.getTabAt(1)).select();
            }
        }
    }
}