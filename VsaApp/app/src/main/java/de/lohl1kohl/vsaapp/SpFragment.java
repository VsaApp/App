package de.lohl1kohl.vsaapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static de.lohl1kohl.vsaapp.MainActivity.firstOpen;


public class SpFragment extends Fragment {
    private Activity mainActivity;
    private View spView;
    private Server server = new Server();
    private Map<String, String> subjectsSymbols = new HashMap<>();

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

        Log.i("firstOpen", String.valueOf(firstOpen));
        if (firstOpen) {
            // Try to refresh the sp...
            syncSp();
            firstOpen = false;
        } else {
            // Show saved sp...
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            String savedSP = sharedPref.getString("pref_sp", "-1");

            if (!savedSP.equals("-1")) {
                try {
                    fillSp(savedSP);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return spView;
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
                try {
                    fillSp(output);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("VsaApp/Server", "Success");

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
                // Show saved sp...
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                String savedSP = sharedPref.getString("pref_sp", "-1");

                if (!savedSP.equals("-1")) {
                    try {
                        fillSp(savedSP);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // Send request to server...
        server.updateSp(classname, callback);

    }

    public void fillSp(String spData) throws JSONException {
        // Get current subjects...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String grade = sharedPref.getString("pref_grade", "-1");
        Log.i("VsaApp/fillSp", grade);
        Log.i("spData", spData);
        ViewPager pager = spView.findViewById(R.id.sp_viewpager);
        pager.setAdapter(new DayAdapter(getFragmentManager(), new JSONArray(spData), subjectsSymbols));
        TabLayout tabLayout = spView.findViewById(R.id.sp_tabs);
        tabLayout.setupWithViewPager(pager);
    }
}