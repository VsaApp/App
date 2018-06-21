package de.lohl1kohl.vsaapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

import de.lohl1kohl.vsaapp.holder.Callbacks;
import de.lohl1kohl.vsaapp.holder.SpHolder;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    public void onNoSp(JSONArray changes, String weekday) throws JSONException {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < changes.length(); i++) {
            JSONObject change = changes.getJSONObject(i);
            text.append(change.getString("unit")).append(". Stunde ").append(change.getJSONObject("changed").getString("teacher")).append(" ").append(change.getJSONObject("changed").getString("info")).append(" ").append(change.getJSONObject("changed").getString("room")).append("\n");
        }
        this.notifyUser(weekday, text.toString());
    }

    public void notifyUser(String title, String text) {
        if (text.length() != 0) {
            text = text.substring(0, text.length() - 1);

            String tText = text;
            if (tText.split("\n").length > 1) {
                tText = tText.split("\n").length + " Änderungen";
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("page", "vp");
            intent.putExtra("day", title);
            Random generator = new Random();

            PendingIntent i = PendingIntent.getActivity(getApplicationContext(), generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), String.valueOf(generator.nextInt()))
                    .setSmallIcon(R.mipmap.logo_white)
                    .setContentTitle(title)
                    .setContentText(tText)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setVibrate(new long[]{250, 250, 250, 250})
                    .setContentIntent(i);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(notificationManager).notify(generator.nextInt(), builder.build());
        }

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("data"));
            Log.i("JSON", jsonObject.toString());
            String weekday = jsonObject.getString("weekday");
            JSONArray changes = jsonObject.getJSONArray("changes");
            FirebaseMessagingService service = this;
            SpHolder.load(getApplicationContext(), false, new Callbacks.spLoadedCallback() {
                @Override
                public void onOldLoaded() {
                    try {
                        service.onSp(changes, weekday);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNoSp() {
                    try {
                        service.onNoSp(changes, weekday);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNewLoaded() {

                }

                @Override
                public void onConnectionFailed() {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onSp(JSONArray changes, String weekday) throws JSONException {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < changes.length(); i++) {
            JSONObject change = changes.getJSONObject(i);
            Log.i("Change", change.toString());
            int day = 0;
            switch (weekday) {
                case "Montag":
                    day = SpHolder.MONDAY;
                    break;
                case "Dienstag":
                    day = SpHolder.TUESDAY;
                    break;
                case "Mittwoch":
                    day = SpHolder.WEDNESDAY;
                    break;
                case "Donnerstag":
                    day = SpHolder.THURSDAY;
                    break;
                case "Freitag":
                    day = SpHolder.FRIDAY;
                    break;
            }
            try {
                Lesson lesson = SpHolder.getLesson(day, Integer.parseInt(change.getString("unit")) - 1);
                Subject subject = lesson.getSubject();
                String normal = subject.name;
                String changed = change.getString("lesson").split(" ")[0];
                Log.i(normal, changed);
                if (normal.equals(changed)) {
                    text.append(change.getString("unit")).append(". Stunde ").append(change.getJSONObject("changed").getString("teacher")).append(" ").append(change.getJSONObject("changed").getString("info")).append(" ").append(change.getJSONObject("changed").getString("room")).append("\n");
                }
            } catch (IndexOutOfBoundsException ignored) {

            }
        }
        this.notifyUser(weekday, text.toString());
    }
}