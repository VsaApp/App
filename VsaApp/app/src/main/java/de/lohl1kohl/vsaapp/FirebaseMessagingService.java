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

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private int channel = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            String title = "";
            String text = "";
            JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("data"));
            Log.i("JSON", jsonObject.toString());
            String weekday = jsonObject.getString("weekday");
            JSONArray changes = jsonObject.getJSONArray("changes");
            if (changes.length() > 1) {
                title = weekday;
                for (int i = 0; i < changes.length(); i++) {
                    JSONObject change = changes.getJSONObject(i);
                    Log.i("Change", change.toString());
                    text += change.getString("unit") + ". Stunde " + change.getJSONObject("changed").getString("tutor") + " " + change.getJSONObject("changed").getString("info") + " " + change.getJSONObject("changed").getString("room") + "\n";
                }
                text = text.substring(0, text.length() - 1);
            } else {
                title = weekday + " " + changes.getJSONObject(0).getString("unit") + ". Stunde";
                text = changes.getJSONObject(0).getJSONObject("changed").getString("tutor") + " " + changes.getJSONObject(0).getJSONObject("changed").getString("info") + " " + changes.getJSONObject(0).getJSONObject("changed").getString("room");
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("page", "vp");
            intent.putExtra("day", weekday);
            Random generator = new Random();

            PendingIntent i = PendingIntent.getActivity(getApplicationContext(), generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), String.valueOf(channel))
                    .setSmallIcon(R.mipmap.logo_white)
                    .setContentTitle(title)
                    .setContentText(text.split("\n")[0])
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setVibrate(new long[]{500, 500})
                    .setContentIntent(i);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(notificationManager).notify(channel++, builder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
