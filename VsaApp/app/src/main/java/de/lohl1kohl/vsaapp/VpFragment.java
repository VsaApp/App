package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
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


public class VpFragment extends Fragment {
    Activity mainActivity;
    VpAdapter vpAdapter;
    View vpView;
    Server server = new Server();
    private Map<String, String> subjectsSymbols = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vpView = inflater.inflate(R.layout.fragment_vp, container, false);
        mainActivity = getActivity();

        // Update vp...
        syncVp();

        // Add click listener...
        ListView listView = vpView.findViewById(R.id.vpList);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            Lesson clickedLesson = vpAdapter.getLesson(position);
            showVpInfoDialog(clickedLesson);
        });


        // Create dictionary with all subject symbols...
        String[] subjects = getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");

            subjectsSymbols.put(pair[0], pair[1]);
        }

        // Add refresh listener...
        SwipeRefreshLayout swipeLayout = vpView.findViewById(R.id.vpListLayout);
        swipeLayout.setOnRefreshListener(this::syncVp);

        return vpView;
    }

    private void syncVp() {
        // Get classname...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String classname = sharedPref.getString("pref_grade", "-1");

        // Check if a classname is set...
        if (classname.equals("-1")) {
            Toast.makeText(mainActivity, R.string.no_class, Toast.LENGTH_LONG).show();
            return;
        }

        // Create callback...
        Server.vpCallback callback = new Server.vpCallback() {
            @Override
            public void onReceived(String output) {
                fillVp(output);
                Log.v("VsaApp/Server", "Success");
                SwipeRefreshLayout swipeLayout = vpView.findViewById(R.id.vpListLayout);
                swipeLayout.setRefreshing(false);

                // Save the current sp...
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pref_vp", output);
                editor.apply();
            }

            @Override
            public void onConnectionFailed() {
                Log.e("VsaApp/Server", "Failed");
                Toast.makeText(mainActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();
                SwipeRefreshLayout swipeLayout = vpView.findViewById(R.id.vpListLayout);

                // Show saved sp...
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                String savedVP = sharedPref.getString("pref_vp", "-1");

                if (!savedVP.equals("-1")) {
                    fillVp(savedVP);
                }

                swipeLayout.setRefreshing(false);
            }
        };

        // Send request to server...
        server.updateVp(classname, callback, "today");
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

    private void fillVp(String output) {
        ArrayList<Lesson> lessons = new ArrayList<>();

        try {
            String weekday = "";
            String date = "";
            String time = "";

            JSONArray jsonarray = new JSONArray(output);
            if (jsonarray.length() == 0) {
                TextView textView = vpView.findViewById(R.id.vpStand);
                textView.setText(R.string.no_vp);
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

            TextView textView = vpView.findViewById(R.id.vpStand);
            String[] parts = date.substring(0, 10).split("-");
            date = parts[2] + "." + parts[1] + "." + parts[0];
            textView.setText(String.format("Für %s den %s (Von: %s)", weekday, date, time));

        } catch (JSONException e) {
            Log.i("VsaApp/SpFragment", "Cannont convert output to array!");
        }

        ListView gridview = vpView.findViewById(R.id.vpList);
        vpAdapter = new VpAdapter(mainActivity, lessons);
        gridview.setAdapter(vpAdapter);
    }

    @SuppressLint("SetTextI18n")
    private void showVpInfoDialog(Lesson lesson) {
        final Dialog loginDialog = new Dialog(mainActivity);
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
        List<String> shortNames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.short_names)));
        List<String> longNames = new ArrayList<>(Arrays.asList(getContext().getResources().getStringArray(R.array.long_names)));

        if (tutorNormal.length() > 0) {
            if (shortNames.contains(lesson.tutor)) {
                tutorNormal = longNames.get(shortNames.indexOf(tutorNormal));
                tutorNormal = tutorNormal.replace("Herr", "Herrn");
            }
        }

        if (tutorNow.length() > 0) {
            if (shortNames.contains(lesson.tutor)) {
                tutorNow = longNames.get(shortNames.indexOf(tutorNow));
                tutorNow = tutorNow.replace("Herr", "Herrn");
            }
        }

        // Get all TextViews...
        final TextView tV_units = loginDialog.findViewById(R.id.lbl_unit);
        final TextView tV_normal = loginDialog.findViewById(R.id.lbl_normal);
        final TextView tV_changed = loginDialog.findViewById(R.id.lbl_changed);


        tV_units.setText(Integer.toString(lesson.unit) + ". Stunde");
        tV_normal.setText(String.format("Mit %s %s in Raum %s", tutorNormal, lesson.getName(), lesson.room));

        String text;
        if (lesson.changes.tutor.length() > 0)
            text = String.format("Jetzt mit %s: %s", tutorNow, lesson.changes.name);
        else text = String.format("Jetzt %s", lesson.changes.getName());
        if (lesson.changes.room.length() > 0) text += " im Raum " + lesson.changes.room;
        tV_changed.setText(text);

        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }
}