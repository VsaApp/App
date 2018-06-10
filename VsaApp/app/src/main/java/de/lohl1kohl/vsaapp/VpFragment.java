package de.lohl1kohl.vsaapp;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class VpFragment extends Fragment {
    Activity mainActivity;
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
                String savedSP = sharedPref.getString("pref_sp", "-1");

                if (!savedSP.equals("-1")) {
                    fillVp(savedSP);
                }

                swipeLayout.setRefreshing(false);
            }
        };

        // Send request to server...
        server.updateVp(classname, callback, "today");
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
                JSONObject changed = new JSONObject(entry.getString("changed"));

                String info = changed.getString("info");
                String tutor = changed.getString("tutor");
                String room = changed.getString("room");


                Lesson lesson = new Lesson(date, unit, "Deutsch", "222", "Egl", subjectsSymbols);
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
        VpAdapter vpAdapter = new VpAdapter(mainActivity, lessons);
        gridview.setAdapter(vpAdapter);
    }
}