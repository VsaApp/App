package de.lohl1kohl.vsaapp.fragments.settings;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.job.JobManager;

import org.json.JSONException;

import de.lohl1kohl.vsaapp.FirebaseHandler;
import de.lohl1kohl.vsaapp.LoadingActivity;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BasePreferenceFragment;
import de.lohl1kohl.vsaapp.fragments.vp.VpFragment;
import de.lohl1kohl.vsaapp.holders.SpHolder;
import de.lohl1kohl.vsaapp.holders.VpHolder;

import static de.lohl1kohl.vsaapp.fragments.web.WebFragment.pushChoices;

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
        Preference clearCache = findPreference("pref_clear_cache");
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                sharedPref.edit().remove("pref_sums").remove("pref_cafetoria").commit();
                Intent mStartActivity = new Intent(mActivity, LoadingActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(mActivity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, 0, mPendingIntent);
                System.exit(0);
                return true;
            }
        });
        Preference mutePhone = findPreference("pref_mutePhone");
        mutePhone.setOnPreferenceChangeListener((preference, o) -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            if (sharedPreferences.getBoolean("pref_mutePhone", false)) {
                final AudioManager mode = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("in_school", false);
                editor.apply();
                mode.setRingerMode(sharedPreferences.getInt("ringer_mode", AudioManager.RINGER_MODE_VIBRATE));
                NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(-1);
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
        });

        Preference gradePref = findPreference("pref_grade");
        gradePref.setOnPreferenceClickListener(preference -> {
            boolean connected = false;
            ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        connected = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        connected = true;
            }

            if (connected) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                int currentIndex = 0;
                String[] grades = getResources().getStringArray(R.array.nameOfGrades);
                for (int i = 0; i < grades.length; i++)
                    if (grades[i].equals(sharedPref.getString("pref_grade", null)))
                        currentIndex = i;
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mActivity);
                builder.setTitle(getString(R.string.choose_grade));
                builder.setSingleChoiceItems(grades, currentIndex, (dialog, which) -> {
                    dialog.cancel();
                    // Save settings...
                    sharedPref.edit().putString("pref_grade", grades[which]).commit();
                    // Update sp, vp and firebase...
                    FirebaseHandler.unsubscribeAll(mActivity.getApplicationContext());
                    FirebaseHandler.subscribe(mActivity.getApplicationContext(), grades[which]);
                    new Thread(this::reloadData).start();
                    TextView headerName = mActivity.findViewById(R.id.header_name);
                    headerName.setText(String.format("%s - %s", mActivity.getResources().getString(R.string.app_name), grades[which]));
                });

                builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                    dialog.cancel();
                });

                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                Toast.makeText(mActivity, getString(R.string.changeGradeOnlyWithNetworkConnection), Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    private void reloadData() {
        SpHolder.load(mActivity, true, () -> {
            Log.d("VsaApp/LoadingActivity", "SpHolder loaded");

            // Load Vp...
            VpHolder.load(mActivity, true, () -> {
                if (mActivity.getIntent().getStringExtra("day") != null) {
                    VpFragment.selectDay(mActivity.getIntent().getStringExtra("day"));
                }
                try {
                    pushChoices(mActivity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("VsaApp/LoadingActivity", "VpHolder loaded");
            });
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("pref_showVpOnlyForYou")) {
            new Thread(() -> VpHolder.load(mActivity, false)).start();
        }
    }
}
