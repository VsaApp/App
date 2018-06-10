package de.lohl1kohl.vsaapp;

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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SpFragment extends Fragment {
    private SpAdapter spAdapter;
    private Activity mainActivity;
    private View spView;
    private Server server = new Server();
    private Map<String, String> subjectsSymbols = new HashMap<String, String>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        spView = inflater.inflate(R.layout.fragment_sp, container, false);

        mainActivity = getActivity();

        // Create dictionary with all subject symbols...
        String[] subjects = getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");

            subjectsSymbols.put(pair[0], pair[1]);
        }

        // Try to refresh the sp...
        syncSp();

        SwipeRefreshLayout swipeLayout = spView.findViewById(R.id.spLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                             @Override
                                             public void onRefresh() {
                                                 syncSp();
                                             }
                                         }
        );

        return spView;
    }

    private void showChooseSubjectDialog(ArrayList<Lesson> subjects) {
        final Dialog loginDialog = new Dialog(mainActivity);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(Objects.requireNonNull(loginDialog.getWindow()).getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_choose_subjects);
        loginDialog.setCancelable(false);
        loginDialog.setTitle(R.string.chooseSubjectDialog);

        final TextView textView_subjectInfo = loginDialog.findViewById(R.id.textView_subjectInfo);
        final GridView chooseSubjectGrid = loginDialog.findViewById(R.id.chooseSubjectGrid);
        chooseSubjectGrid.setAdapter(new SpAdapter(mainActivity, subjects, true));
        Map<String, String> days = new HashMap<String, String>();
        days.put("Mo", "Montag");
        days.put("Di", "Dienstag");
        days.put("Mi", "Mittwoch");
        days.put("Do", "Donnerstag");
        days.put("Fr", "Freitag");
        textView_subjectInfo.setText("Welches Fach haben sie " + days.get(subjects.get(0).day) + " in der " + subjects.get(0).unit + ". Stunde?");

        chooseSubjectGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("VsaApp/SpFragment", Integer.toString(i));
                Lesson selectedLesson = subjects.get(i);

                // Get current subjects...
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                String grade = sharedPref.getString("pref_grade", "");
                String choosedSubjects = sharedPref.getString("pref_choosedSubjects" + grade, "-1");

                // Open settings to edit them...
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();

                // Edit settings...
                if (choosedSubjects.equals("-1")) {
                    editor.putString("pref_choosedSubjects" + grade, selectedLesson.name);
                } else {
                    editor.putString("pref_choosedSubjects" + grade, choosedSubjects + ":" + selectedLesson.name);
                }
                editor.apply();

                loginDialog.cancel();

                fillSp(sharedPref.getString("pref_sp", "-1"));
            }
        });

        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }

    public void syncSp() {
        // Get classname...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String classname = sharedPref.getString("pref_grade", "-1");

        // Check if a classname is set...
        if (classname.equals("-1")) {
            Toast.makeText(mainActivity, R.string.no_class, Toast.LENGTH_LONG).show();
            return;
        }

        // Create callback...
        Server.spCallback callback = new Server.spCallback() {
            @Override
            public void onReceived(String output) {
                fillSp(output);
                Log.i("VsaApp/Server", "Success");
                SwipeRefreshLayout swipeLayout = spView.findViewById(R.id.spLayout);
                swipeLayout.setRefreshing(false);

                // Save the current sp...
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pref_sp", output);
                editor.apply();
            }

            @Override
            public void onConnectionFailed() {
                Log.e("VsaApp/Server", "Failed");
                Toast.makeText(mainActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();
                SwipeRefreshLayout swipeLayout = spView.findViewById(R.id.spLayout);

                // Show saved sp...
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                String savedSP = sharedPref.getString("pref_sp", "-1");

                if (!savedSP.equals("-1")) {
                    fillSp(savedSP);
                }

                swipeLayout.setRefreshing(false);
            }
        };

        // Send request to server...
        server.updateSp(classname, callback);

    }

    private void fillSp(String spData) {
        // Get current subjects...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String grade = sharedPref.getString("pref_grade", "");
        ArrayList<String> choosedSubjects = new ArrayList<String>();
        Log.i("VsaApp/fillSp", grade);
        Collections.addAll(choosedSubjects, sharedPref.getString("pref_choosedSubjects" + grade, "").split(":"));

        ArrayList<ArrayList<Lesson>> lessonsObjects = new ArrayList<ArrayList<Lesson>>();

        try {
            JSONArray jsonarray = new JSONArray(spData);
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject day = jsonarray.getJSONObject(i);
                String name = day.getString("name");
                JSONArray lessons = new JSONArray(day.getString("lessons"));
                ArrayList<Lesson> dayObjects = new ArrayList<Lesson>();

                for (int j = 0; j < lessons.length(); j++) {
                    JSONArray lesson = lessons.getJSONArray(j);

                    ArrayList<Lesson> lessonObjects = new ArrayList<Lesson>();

                    for (int x = 0; x < lesson.length(); x++) {
                        JSONObject subject = lesson.getJSONObject(x);
                        String subjectName = subject.getString("lesson");
                        String room = subject.getString("room");
                        String tutor = subject.getString("tutor");
                        Lesson lessonObject = new Lesson(name, j + 1, subjectName, room, tutor, subjectsSymbols);
                        lessonObjects.add(lessonObject);
                    }

                    if (lessonObjects.size() > 0) {
                        if (lessonObjects.size() > 1) {
                            boolean olreadyChoosed = false;
                            for (Lesson lesson1 : lessonObjects) {
                                if (choosedSubjects.contains(lesson1.name)) {
                                    olreadyChoosed = true;
                                    dayObjects.add(lessonObjects.get(lessonObjects.indexOf(lesson1)));
                                }
                            }

                            if (!olreadyChoosed) {
                                showChooseSubjectDialog(lessonObjects);
                            }
                        } else dayObjects.add(lessonObjects.get(0));
                    }
                }

                lessonsObjects.add(dayObjects);
            }
        } catch (JSONException e) {
            Log.i("VsaApp/SpFragment", "Cannont convert output to array!");
        }


        GridView gridview = spView.findViewById(R.id.spGrid);
        spAdapter = new SpAdapter(mainActivity, lessonsObjects);
        gridview.setAdapter(spAdapter);
    }
}