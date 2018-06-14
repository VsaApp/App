package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import de.lohl1kohl.vsaapp.server.Callbacks;
import de.lohl1kohl.vsaapp.server.Sp;

public class SettingsFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        Preference button = findPreference("pref_logout");
        button.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.really_logout)
                    .setIcon(R.mipmap.logo_transparent)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        sharedPref.edit().clear().commit();
                        Intent mStartActivity = new Intent(mActivity, MainActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(mActivity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
                        Objects.requireNonNull(mgr).set(AlarmManager.RTC, 0, mPendingIntent);
                        System.exit(0);
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            return true;
        });

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_grade")) {
            syncSp();
        }
    }

    @SuppressLint("SetTextI18n")
    public void syncSp() {

        // Get gradename...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String gradename = sharedPref.getString("pref_grade", "-1");

        // Check if a gradename is set...
        if (gradename.equals("-1")) {
            Toast.makeText(mActivity, R.string.no_class, Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseHandler.unsubscribeAll(mActivity.getApplicationContext());
        FirebaseHandler.subscribe(mActivity.getApplicationContext(), gradename);

        TextView textView = mActivity.findViewById(R.id.header_name);
        textView.setText(mActivity.getResources().getString(R.string.app_name) + " - " + gradename);

        // Create callback...
        Callbacks.spCallback callback = new Callbacks.spCallback() {
            @Override
            public void onReceived(String output) {
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
            }

            @Override
            public void onNoSp() {

            }
        };

        new Sp().updateSp(gradename, callback);
    }
}