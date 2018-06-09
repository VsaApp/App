package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.TextView;

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
            // Update subscription for grade
            FirebaseHandler.unsubscribeAll(this.getActivity().getApplicationContext());
            FirebaseHandler.subscribe(this.getActivity().getApplicationContext(), sharedPreferences.getString("pref_grade", "-1"));
            ((TextView) getActivity().findViewById(R.id.header_name)).setText(getString(R.string.app_name) + " - " + sharedPreferences.getString("pref_grade", "-1"));
        }
    }
}