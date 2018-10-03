package de.lohl1kohl.vsaapp.fragments.pinboard;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.lohl1kohl.vsaapp.LoadingActivity;
import de.lohl1kohl.vsaapp.R;

import java.util.Calendar;
import java.util.Date;

public class PinBoardActivity extends AppCompatActivity {

    LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinboard);
        String user = getIntent().getStringExtra("user");
        TextView username = findViewById(R.id.activity_pin_board_username);
        username.setText(user);
        list = findViewById(R.id.activity_pin_board_message_list);
        PinBoardMessagesHolder.load(this, user, true, () -> displayMessages(user));
    }

    public void displayMessages(String user) {
        list.removeAllViews();
        for (PinBoardMessagesHolder.Message message : PinBoardMessagesHolder.getMessages(user)) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View root = inflater.inflate(R.layout.pinboard_message_cell, null, false);
            ((TextView) root.findViewById(R.id.pinboard_message_cell_title)).setText(message.getTitle());
            Date date = new Date(message.getTime() * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String year = String.valueOf(cal.get(Calendar.YEAR));
            String month = String.valueOf(cal.get(Calendar.MONTH));
            String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            String minute = String.valueOf(cal.get(Calendar.MINUTE));
            String second = String.valueOf(cal.get(Calendar.SECOND));
            if (month.length() == 1) {
                month = "0" + month;
            }
            if (day.length() == 1) {
                day = "0" + day;
            }
            if (hour.length() == 1) {
                hour = "0" + hour;
            }
            if (minute.length() == 1) {
                minute = "0" + minute;
            }
            if (second.length() == 1) {
                second = "0" + second;
            }
            String time = day + "." + month + "." + year + " " + hour + ":" + minute + ":" + second;
            ((TextView) root.findViewById(R.id.pinboard_message_cell_date_time)).setText(time);
            ((TextView) root.findViewById(R.id.pinboard_message_cell_message)).setText(message.getMessage());
            list.addView(root);
        }
        try {
            list.getChildAt(list.getChildCount() - 1).findViewById(R.id.line).setVisibility(View.GONE);
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("notification", false)) {
            startActivity(new Intent(this, LoadingActivity.class));
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(10);
    }
}
