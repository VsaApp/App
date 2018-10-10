package de.lohl1kohl.vsaapp.jobs;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import de.lohl1kohl.vsaapp.LoadingActivity;
import de.lohl1kohl.vsaapp.holders.SpHolder;

public class EndJob extends Job {

    public static final String TAG = "endjob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final AudioManager mode = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mode.setRingerMode(sharedPreferences.getInt("ringer_mode", AudioManager.RINGER_MODE_VIBRATE));

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(-1);

        SpHolder.load(getContext(), false, () -> LoadingActivity.createJob(getContext()));
        return Result.SUCCESS;
    }
}
