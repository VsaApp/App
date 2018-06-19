package de.lohl1kohl.vsaapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private Map<String, String> subjectsSymbols = new HashMap<>();

    @Override
    public void onCreate() {
        String[] subjects = getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");

            subjectsSymbols.put(pair[0], pair[1]);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            String title;
            StringBuilder text = new StringBuilder();
            JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("data"));
            Log.i("JSON", jsonObject.toString());
            String weekday = jsonObject.getString("weekday");
            JSONArray changes = jsonObject.getJSONArray("changes");
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            for (int i = 0; i < changes.length(); i++) {
                JSONObject change = changes.getJSONObject(i);
                String prefName = String.format("pref_selectedSubject%s:%s:%s", settings.getString("pref_grade", "-1"), weekday, Integer.parseInt(change.getString("unit")) - 1);
                String prefValue = settings.getString(prefName, "-1");
                boolean display = false;
                if (!prefValue.equals("-1")) {
                    Log.i(prefName, prefValue);
                    String subject = prefValue.split(":")[0];
                    String tutor = prefValue.split(":")[1];
                    String changedTutor = change.getJSONObject("changed").getString("tutor");
                    if (changedTutor.equals("")) {
                        changedTutor = tutor;
                    }
                    Log.i(tutor, changedTutor);
                    Log.i(change.getString("lesson"), subject);
                    if (changedTutor.equals(tutor) && change.getString("lesson").equals(subject)) {
                        display = true;
                    }
                }
                if (display) {
                    text.append(change.getString("unit")).append(". Stunde ").append(change.getJSONObject("changed").getString("tutor")).append(" ").append(change.getJSONObject("changed").getString("info")).append(" ").append(change.getJSONObject("changed").getString("room")).append("\n");
                }
            }

            title = weekday;
            if (text.length() != 0) {
                text = new StringBuilder(text.substring(0, text.length() - 1));

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("page", "vp");
                intent.putExtra("day", weekday);
                Random generator = new Random();

                PendingIntent i = PendingIntent.getActivity(getApplicationContext(), generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), String.valueOf(generator.nextInt()))
                        .setSmallIcon(R.mipmap.logo_white)
                        .setContentTitle(title)
                        .setContentText(text.toString().split("\n")[0])
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text.toString()))
                        .setVibrate(new long[]{500, 500})
                        .setContentIntent(i);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Objects.requireNonNull(notificationManager).notify(generator.nextInt(), builder.build());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}