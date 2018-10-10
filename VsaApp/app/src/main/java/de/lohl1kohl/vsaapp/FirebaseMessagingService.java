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

import java.util.List;
import java.util.Random;

import de.lohl1kohl.vsaapp.fragments.sp.Lesson;
import de.lohl1kohl.vsaapp.fragments.sp.Subject;
import de.lohl1kohl.vsaapp.holders.SpHolder;
import de.lohl1kohl.vsaapp.holders.SubjectSymbolsHolder;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    public void notifyUser(String title, String text) {
        if (text.length() == 0) {
            text = getString(R.string.no_changes);
        }else text = text.substring(0, text.length() - 1);

        String tText = text;
        if (tText.split("\n").length > 1) {
            tText = tText.split("\n").length + " Änderungen";
        }

        int day = 0;
        switch (title) {
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

        Intent intent = new Intent(this, LoadingActivity.class);
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
        notificationManager.notify(day, builder.build());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("data"));
            String weekday = jsonObject.getString("weekday");
            JSONArray changes = jsonObject.getJSONArray("changes");
            FirebaseMessagingService service = this;
            SubjectSymbolsHolder.load(this);
            SpHolder.load(getApplicationContext(), false, () -> {
                try {
                    service.onSp(changes, weekday);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onSp(JSONArray changes, String weekday) throws JSONException {
        Log.i("changes", changes.toString());
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < changes.length(); i++) {
            JSONObject change = changes.getJSONObject(i);
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
                String normal = subject.name.toLowerCase();
                String changed = change.getString("lesson").split(" ")[0].toLowerCase();
                String teacher = change.getString("teacher").toLowerCase();
                Log.i("VsaApp/MsgService", String.format("Normal: %s, Changed: %s, Teacher: %s, Info: %s", normal, changed, teacher, change.getJSONObject("changed").getString("info").toLowerCase()));
                if (change.getJSONObject("changed").getString("info").toLowerCase().contains("klausur")) {
                    boolean isMyExam = false;
                    for (int j = 0; j < 5; j++) {
                        List<Lesson> dayOfWeek = SpHolder.getDay(j);
                        for (int k = 0; k < dayOfWeek.size(); k++) {
                            Lesson lessonOfDay = dayOfWeek.get(k);
                            if (lessonOfDay.numberOfSubjects() > 0) {
                                Subject subjectOfLesson = lessonOfDay.getSubject();
                                if (teacher.equals(subjectOfLesson.teacher.toLowerCase()) && changed.equals(subjectOfLesson.name.toLowerCase()))
                                    isMyExam = true;
                            }
                        }
                    }
                    if (isMyExam)
                        text.append(change.getString("unit")).append(". Stunde ").append(change.getJSONObject("changed").getString("teacher")).append(" ").append(change.getJSONObject("changed").getString("info")).append(" ").append(change.getJSONObject("changed").getString("room")).append("\n");


                } else if (normal.equals(changed) || (normal.equals(getString(R.string.lesson_tandem).toLowerCase()) && (changed.equals(getString(R.string.lesson_french).toLowerCase()) || changed.equals(getString(R.string.lesson_latin).toLowerCase())))) {
                    text.append(change.getString("unit")).append(". Stunde ").append(change.getJSONObject("changed").getString("teacher")).append(" ").append(change.getJSONObject("changed").getString("info")).append(" ").append(change.getJSONObject("changed").getString("room")).append("\n");
                }
            } catch (IndexOutOfBoundsException ignored) {

            }
        }
        this.notifyUser(weekday, text.toString());
    }
}