package de.lohl1kohl.vsaapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private int channel = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getData().get("title");
        String text = remoteMessage.getData().get("text");

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("page", "vp");
        Random generator = new Random();

        PendingIntent i = PendingIntent.getActivity(getApplicationContext(), generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), String.valueOf(channel))
                .setSmallIcon(R.drawable.logo_white)
                .setContentTitle(title)
                .setContentText(text.split("\n")[0])
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setVibrate(new long[]{500, 500})
                .setContentIntent(i);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).notify(channel++, builder.build());
    }
}
