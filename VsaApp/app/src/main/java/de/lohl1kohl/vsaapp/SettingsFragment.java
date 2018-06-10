package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_grade")) {
            if (this.getActivity() != null) {
                // Update subscription for grade
                FirebaseHandler.unsubscribeAll(this.getActivity().getApplicationContext());
                FirebaseHandler.subscribe(this.getActivity().getApplicationContext(), sharedPreferences.getString("pref_grade", "-1"));

                Intent mStartActivity = new Intent(this.getActivity().getApplicationContext(), MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(this.getActivity().getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) this.getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Objects.requireNonNull(mgr).set(AlarmManager.RTC, 0, mPendingIntent);
                System.exit(0);
            }
        }
    }
}