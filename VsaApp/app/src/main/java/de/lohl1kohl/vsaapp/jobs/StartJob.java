package de.lohl1kohl.vsaapp.jobs;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.lohl1kohl.vsaapp.LoadingActivity;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.sp.Lesson;
import de.lohl1kohl.vsaapp.fragments.sp.LessonUtils;
import de.lohl1kohl.vsaapp.holders.SpHolder;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class StartJob extends Job {

    public static final String TAG = "startjob";

    private int secondsUntilEnd(java.util.Date end) {
        final long millis = end.getTime() - System.currentTimeMillis();
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return (int) (hours * 3600 + minutes * 60 + seconds);
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Callbacks.baseLoadedCallback baseLoadedCallback = () -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("in_school", true);
            editor.apply();
            final AudioManager mode = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);


            Intent intent = new Intent(getContext(), LoadingActivity.class);
            Random generator = new Random();

            PendingIntent i = PendingIntent.getActivity(getContext(), generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "muted")
                    .setSmallIcon(R.mipmap.logo_white)
                    .setContentTitle(getContext().getString(R.string.in_school))
                    .setContentText(getContext().getString(R.string.phone_muted))
                    .setColor(getContext().getResources().getColor(R.color.colorPrimary))
                    .setOngoing(true)
                    .setContentIntent(i);

            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(-1, builder.build());


            Date now = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            List<Lesson> lessons = SpHolder.getDay(cal.get(Calendar.DAY_OF_WEEK) - 2);
            cal.set(Calendar.HOUR_OF_DAY, 7);
            cal.set(Calendar.MINUTE, 50);
            cal.set(Calendar.SECOND, 0);
            cal.add(Calendar.MINUTE, LessonUtils.endTimes[lessons.size() - 1] + 20); // End time of last lesson + 10 minutes ( + 10 minutes for 7:50am to 8:00am)

            if (now.before(cal.getTime())) {
                int end = secondsUntilEnd(cal.getTime()) * 1000;
                editor.putInt("endid",
                        new JobRequest.Builder(EndJob.TAG)
                                .setExact(end)
                                .setUpdateCurrent(true)
                                .build()
                                .schedule()
                );
            }
        };
        SpHolder.load(getContext(), false, baseLoadedCallback);
        return Result.SUCCESS;
    }
}
