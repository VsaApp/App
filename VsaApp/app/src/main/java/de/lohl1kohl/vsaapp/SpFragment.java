package de.lohl1kohl.vsaapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.lohl1kohl.vsaapp.server.Callbacks;
import de.lohl1kohl.vsaapp.server.Sp;

import static de.lohl1kohl.vsaapp.MainActivity.firstOpen;


public class SpFragment extends BaseFragment {
    private View spView;
    private Map<String, String> subjectsSymbols = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        spView = inflater.inflate(R.layout.fragment_sp, container, false);


        // Create dictionary with all subject symbols...
        String[] subjects = getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");

            subjectsSymbols.put(pair[0], pair[1]);
        }

        // Try to refresh the sp...
        syncSp();

        return spView;
    }

    public void syncSp() {
        // Get gradename...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String gradename = sharedPref.getString("pref_grade", "-1");

        // Check if a gradename is set...
        if (gradename.equals("-1")) {
            Toast.makeText(mActivity, R.string.no_class, Toast.LENGTH_LONG).show();
            return;
        }

        // Create callback...
        Callbacks.spCallback callback = new Callbacks.spCallback() {
            @Override
            public void onReceived(String output) {
                try {
                    fillSp(output);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("VsaApp/Server", "Success");

                // Save the current sp...
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String grade = sharedPref.getString("pref_grade", "-1");
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pref_sp_" + grade, output);
                editor.apply();
            }

            @Override
            public void onConnectionFailed() {
                Log.e("VsaApp/Server", "Failed");
                Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();
                // Show saved sp...
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String grade = sharedPref.getString("pref_grade", "-1");
                String savedSP = sharedPref.getString("pref_sp_" + grade, "-1");

                if (!savedSP.equals("-1")) {
                    try {
                        fillSp(savedSP);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (firstOpen) {
            // Send request to server...
            new Sp().updateSp(gradename, callback);
            firstOpen = false;
        } else {
            // Show saved sp...
            String grade = sharedPref.getString("pref_grade", "-1");
            String savedSP = sharedPref.getString("pref_sp_" + grade, "-1");

            if (!savedSP.equals("-1")) {
                try {
                    fillSp(savedSP);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void fillSp(String spData) throws JSONException {
        // Get current subjects...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");
        Log.i("VsaApp/fillSp", grade);
        Log.i("spData", spData);
        ViewPager pager = spView.findViewById(R.id.sp_viewpager);
        SpDayAdapter adapter = new SpDayAdapter(getFragmentManager(), new JSONArray(spData), subjectsSymbols);
        pager.setAdapter(adapter);
        TabLayout tabLayout = spView.findViewById(R.id.sp_tabs);
        tabLayout.setupWithViewPager(pager);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (weekday >= 5) {
            weekday = 0;
        }
        TabLayout.Tab tab = tabLayout.getTabAt(weekday);
        Objects.requireNonNull(tab).select();
    }
}