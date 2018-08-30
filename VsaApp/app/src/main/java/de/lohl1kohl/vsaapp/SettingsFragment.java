package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.evernote.android.job.JobManager;

import de.lohl1kohl.vsaapp.holder.SpHolder;
import de.lohl1kohl.vsaapp.holder.VpHolder;

public class SettingsFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        Preference logout = findPreference("pref_logout");
        logout.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.really_logout)
                    .setIcon(R.mipmap.logo_transparent)
                    .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        JobManager.instance().cancel(sharedPref.getInt("startid", 0));
                        JobManager.instance().cancel(sharedPref.getInt("endid", 0));
                        sharedPref.edit().clear().commit();
                        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();
                        Intent mStartActivity = new Intent(mActivity, LoadingActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(mActivity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, 0, mPendingIntent);
                        System.exit(0);
                    })
                    .setNegativeButton(R.string.cancel, null).show();
            return true;
        });
        Preference mutePhone = findPreference("pref_mutePhone");
        mutePhone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                if (sharedPreferences.getBoolean("pref_mutePhone", false)) {
                    final AudioManager mode = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("in_school", false);
                    editor.apply();
                    mode.setRingerMode(sharedPreferences.getInt("ringer_mode", AudioManager.RINGER_MODE_VIBRATE));
                    NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(1);
                    JobManager.instance().cancel(sharedPreferences.getInt("startid", 0));
                    JobManager.instance().cancel(sharedPreferences.getInt("endid", 0));
                } else {
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent mStartActivity = new Intent(mActivity, LoadingActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(mActivity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, 0, mPendingIntent);
                        System.exit(0);
                    }).start();
                }
                return true;
            }
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
            new Thread(() -> VpHolder.load(mActivity)).start();
            TextView headerName = mActivity.findViewById(R.id.header_name);
            headerName.setText(String.format("%s - %s", mActivity.getResources().getString(R.string.app_name), gradename));
        } else if (key.equals("pref_showVpOnlyForYou")) {
            new Thread(() -> VpHolder.load(mActivity)).start();
        }
    }
}
