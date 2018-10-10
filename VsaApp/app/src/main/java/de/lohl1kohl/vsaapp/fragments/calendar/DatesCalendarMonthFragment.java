package de.lohl1kohl.vsaapp.fragments.calendar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.holders.DatesHolder;

public class DatesCalendarMonthFragment extends BaseFragment {

    public DatesFragment mFragment;
    int month;
    int year;
    int columnSize;
    private List<Day> days;
    private LayoutInflater inflater;
    private ViewGroup container;
    private View root;

    private void showEvents(LayoutInflater inflater, List<Event> events, String day) {
        final Dialog eventsDialog = new Dialog(mActivity);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(eventsDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        eventsDialog.setContentView(R.layout.dialog_events);
        eventsDialog.setCancelable(true);
        eventsDialog.setCanceledOnTouchOutside(true);
        eventsDialog.setTitle(day);

        LinearLayout ll = eventsDialog.findViewById(R.id.dialog_events_list);
        Button back = eventsDialog.findViewById(R.id.dialog_events_back);
        back.setOnClickListener(view -> eventsDialog.cancel());
        for (Event event : events) {
            View v = inflater.inflate(R.layout.dates_list_item_event, null);
            View line = v.findViewById(R.id.list_item_line);
            TextView nameView = v.findViewById(R.id.list_event_name);
            TextView timeView = v.findViewById(R.id.list_event_time);

            Color color = event.category.color;
            line.setBackgroundColor(android.graphics.Color.rgb(color.r, color.g, color.b));

            nameView.setText(event.name);

            if (event.start != event.end)
                if (event.start.getHour() != 0)
                    timeView.setText(String.format(mActivity.getResources().getString(R.string.clock), event.start.getHour(), event.end.getHour()));
                else
                    timeView.setText(String.format(mActivity.getResources().getString(R.string.to_day), event.end.getDay(), event.end.getMonth(mActivity), event.end.getYear()));
            else
                timeView.setText(mActivity.getResources().getString(R.string.whole_day));

            v.setOnClickListener(view -> {
                mFragment.editEvent(event);
            });

            ll.addView(v);
        }
        eventsDialog.show();
        eventsDialog.getWindow().setAttributes(lWindowParams);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dates_calendar_month, container, false);

        this.root = root;
        this.inflater = inflater;
        this.container = container;

        new Thread(() -> {
            initDaysArray(mActivity);
            createHeader(inflater, root, container);
        }).start();

        return root;
    }

    public void update() {
        new Thread(() -> {
            initDaysArray(mActivity);
            createGrid(inflater, root, container);
        }).start();
    }

    private void initDaysArray(Context context) {

        // Create days for this month...
        List<Day> monthList = DatesHolder.getMonth(context, month, year);
        int firstWeekday = new Date(1, month + 1, year).getDayOfWeek(context);
        if (firstWeekday > 0) {
            int lastYear = year;
            int lastMonthNumber = month - 1;
            if (lastMonthNumber < 0) {
                lastMonthNumber = 11;
                lastYear = year - 1;
            }
            List<Day> lastMonth = DatesHolder.getMonth(context, lastMonthNumber, lastYear);
            for (int i = 1; i < firstWeekday + 1; i++)
                monthList.add(0, lastMonth.get(lastMonth.size() - i));
        }
        if (monthList.size() < 42) {
            int nextYear = year;
            int nextMonthNumber = month + 1;
            if (nextMonthNumber > 11) {
                nextMonthNumber = 0;
                nextYear = year + 1;
            }
            List<Day> nextMonth = DatesHolder.getMonth(context, nextMonthNumber, nextYear);
            for (int i = 0; i < 42; i++) {
                if (monthList.size() < 42) monthList.add(nextMonth.get(i));
                else break;
            }
        }
        days = monthList;
    }

    private void createHeader(LayoutInflater inflater, View root, ViewGroup container) {

        LinearLayout header = root.findViewById(R.id.dates_calendar_grid_header);

        for (int i = 0; i < 7; i++) {
            View headerItem = inflater.inflate(R.layout.dates_calendar_header, null);

            ViewHolderHeader headerHolder = new ViewHolderHeader();
            headerHolder.weekday = headerItem.findViewById(R.id.weekday_short);
            headerItem.setTag(headerHolder);

            headerHolder.weekday.setText(new ArrayList<>(Arrays.asList(headerItem.getResources().getStringArray(R.array.weekdays))).get(i).substring(0, 2));

            LinearLayout ll = headerItem.findViewById(R.id.header_item);
            columnSize = container.getMeasuredWidth() / 7 + 1;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(columnSize, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.setLayoutParams(params);

            mActivity.runOnUiThread(() -> header.addView(headerItem));
        }

        // After creating the header create the grid...
        createGrid(inflater, root, container);
    }

    private void createGrid(LayoutInflater inflater, View root, ViewGroup container) {
        LinearLayout grid = root.findViewById(R.id.dates_calendar_grid);
        mActivity.runOnUiThread(grid::removeAllViews);

        for (int i = 0; i < 6; i++) {
            View gridLine = inflater.inflate(R.layout.dates_calendar_grid_line, null);
            LinearLayout ll = gridLine.findViewById(R.id.calendar_grid_line);
            int height = grid.getMeasuredHeight() / 6 - 1;
            for (int j = 0; j < 7; j++) {
                View calendarItem = createGridItem(i * 7 + j, inflater, container, height);
                mActivity.runOnUiThread(() -> ll.addView(calendarItem));
            }

            mActivity.runOnUiThread(() -> grid.addView(ll));
        }
    }

    @SuppressLint("SetTextI18n")
    private View createGridItem(int position, LayoutInflater layoutinflater, ViewGroup parent, int height) {
        // Create the view...
        View convertView = layoutinflater.inflate(R.layout.dates_calendar_item, parent, false);
        TextView dayOfMonthInListView = convertView.findViewById(R.id.day_of_month);

        RelativeLayout rl = convertView.findViewById(R.id.dates_calendar_item_layout_l);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(columnSize - 1, height);
        rl.setLayoutParams(params);

        Day day = days.get(position);

        dayOfMonthInListView.setText(Integer.toString(day.day));

        // Get day of week...
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day.day);
        calendar.set(Calendar.MONTH, day.month);
        calendar.set(Calendar.YEAR, day.year);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (weekday == -1 || weekday == 5) {
            dayOfMonthInListView.setTextColor(mActivity.getResources().getColor(R.color.calendarWeekend));
        }

        Calendar today = Calendar.getInstance();
        today.setTime(new java.util.Date());
        if (weekday == -1) weekday = 6;
        String nameOfDay = String.format(mActivity.getString(R.string.name_of_day), Arrays.asList(mActivity.getResources().getStringArray(R.array.weekdays)).get(weekday), day.day, day.month + 1, day.year);

        List<Event> events = day.getEvents();

        if (day.day == today.get(Calendar.DAY_OF_MONTH) && day.month == today.get(Calendar.MONTH) && day.year == today.get(Calendar.YEAR)) {
            RelativeLayout layout = convertView.findViewById(R.id.dates_calendar_item_layout);
            layout.setBackgroundColor(mActivity.getResources().getColor(R.color.today));
            dayOfMonthInListView.setTextColor(mActivity.getResources().getColor(R.color.today));
        }

        if (events.size() > 0) {
            Category category = events.get(0).category;
            rl.setBackgroundColor(android.graphics.Color.rgb(category.color.r, category.color.g, category.color.b));
            convertView.setOnClickListener(view -> showEvents(layoutinflater, events, nameOfDay));
        } else if (month != day.month) {
            rl.setBackgroundColor(mActivity.getResources().getColor(R.color.calendarItemOtherMonth));
        } else {
            if (day.day == today.get(Calendar.DAY_OF_MONTH) && day.month == today.get(Calendar.MONTH) && day.year == today.get(Calendar.YEAR)) {
                rl.setBackgroundColor(mActivity.getResources().getColor(R.color.todayBackground));
            } else rl.setBackgroundColor(mActivity.getResources().getColor(R.color.calendarItem));
        }

        return convertView;
    }

    static class ViewHolderHeader {
        TextView weekday;
    }
}
