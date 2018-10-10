package de.lohl1kohl.vsaapp.jobs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import com.evernote.android.job.JobRequest;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getStringExtra("btn");
        if(action.equals("media")){
            onMedia(context);
        }
        else if(action.equals("ringtone")){
            onRingtone(context);
        }
        else if(action.equals("close")){
            onClose(context);
        }
    }

    public void onClose(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("endid",
                new JobRequest.Builder(EndJob.TAG)
                        .setUpdateCurrent(true)
                        .startNow()
                        .build()
                        .schedule()
        );
        editor.apply();
    }

    public void onMedia(Context context){
        final AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // If the the media is muted, activate it...
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, sharedPreferences.getInt("media_mode", (int) maxVolume / 2), 0);
        }
        else{
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }
    }

    public void onRingtone(Context context){
        final AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // If the the media is muted, activate it...
        if (audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            audio.setRingerMode(sharedPreferences.getInt("ringer_mode", AudioManager.RINGER_MODE_VIBRATE));
        }
        else{
            audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

}
