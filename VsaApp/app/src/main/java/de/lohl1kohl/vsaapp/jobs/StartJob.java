package de.lohl1kohl.vsaapp.jobs;

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
import de.lohl1kohl.vsaapp.LoadingActivity;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.sp.Lesson;
import de.lohl1kohl.vsaapp.fragments.sp.LessonUtils;
import de.lohl1kohl.vsaapp.holders.SpHolder;
import de.lohl1kohl.vsaapp.holders.SubjectSymbolsHolder;
import de.lohl1kohl.vsaapp.loader.Callbacks;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
        SubjectSymbolsHolder.load(getContext());
        Callbacks.baseLoadedCallback baseLoadedCallback = () -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("in_school", true);
            editor.apply();
            final AudioManager audio = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

            createNotification(getContext());

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
                editor.apply();
            }
        };
        SpHolder.load(getContext(), false, baseLoadedCallback);
        return Result.SUCCESS;
    }

    public void createNotification(Context context) {
        final AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audio.getRingerMode();
        int mediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        Intent intent = new Intent(context, LoadingActivity.class);
        Random generator = new Random();

        Intent intent2 = new Intent(context, NotificationReceiver.class);
        intent2.putExtra("btn", "media");

        Intent intent3 = new Intent(context, NotificationReceiver.class);
        intent3.putExtra("btn", "ringtone");

        PendingIntent i = PendingIntent.getActivity(context, generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent media = PendingIntent.getBroadcast(context, generator.nextInt(), intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent ringtone = PendingIntent.getBroadcast(context, generator.nextInt(), intent3, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "muted")
                .setSmallIcon(R.mipmap.logo_white)
                .setContentTitle(context.getString(R.string.in_school))
                .setContentText((ringerMode == AudioManager.RINGER_MODE_SILENT ? context.getString(R.string.phone_muted) : context.getString(R.string.phone_on)) + " + " + (mediaVolume == 0 ? context.getString(R.string.media_muted) : context.getString(R.string.media_on)))
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .addAction(ringerMode == AudioManager.RINGER_MODE_SILENT ? R.drawable.ic_ringtone : R.drawable.ic_volume_mute, "Klingelton", ringtone)
                .addAction(mediaVolume == 0 ? R.drawable.ic_media_volume : R.drawable.ic_volume_off, "Medienton", media)
                .setOngoing(true)
                .setContentIntent(i);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(-1, builder.build());

    }
}
