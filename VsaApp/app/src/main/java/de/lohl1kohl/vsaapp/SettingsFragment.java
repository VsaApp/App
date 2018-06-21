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
import android.widget.TextView;

import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.SpHolder;

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
            String gradename = sharedPreferences.getString("pref_grade", "-1");
            FirebaseHandler.unsubscribeAll(mActivity.getApplicationContext());
            FirebaseHandler.subscribe(mActivity.getApplicationContext(), gradename);
            new Thread(() -> SpHolder.load(mActivity, true)).start();
            TextView headerName = mActivity.findViewById(R.id.header_name);
            headerName.setText(String.format("%s - %s", mActivity.getResources().getString(R.string.app_name), gradename));
        }
    }
}
