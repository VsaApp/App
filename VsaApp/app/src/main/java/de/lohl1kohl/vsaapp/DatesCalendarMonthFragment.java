package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.lohl1kohl.vsaapp.holder.DatesHolder;

public class DatesCalendarMonthFragment extends BaseFragment {

    int month;
    int year;
    private List<Day> days;
    int columSize;
    int rowHeight;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dates_calendar_month, container, false);

        initDaysArray(mActivity);
        createHeader(inflater, root, container);

        return root;
    }

    private void initDaysArray(Context context) {

        // Create days for this month...
        List<Day> monthList = DatesHolder.getMonth(month, year);
        int firstWeekday = new Date(1, month + 1, year).getDayOfWeek(context);
        if (firstWeekday > 0) {
            int lastYear = year;
            int lastMonthNumber = month - 1;
            if (lastMonthNumber < 0) {
                lastMonthNumber = 11;
                lastYear = year - 1;
            }
            List<Day> lastMonth = DatesHolder.getMonth(lastMonthNumber, lastYear);
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
            List<Day> nextMonth = DatesHolder.getMonth(nextMonthNumber, nextYear);
            for (int i = 0; i < 42; i++) {
                if (monthList.size() < 42) monthList.add(nextMonth.get(i));
                else break;
            }
        }
        days = monthList;
    }

    private void createHeader(LayoutInflater inflater, View root, ViewGroup container){

        new Thread(() -> {
            LinearLayout header = root.findViewById(R.id.dates_calendar_grid_header);

            for (int i = 0; i < 7; i++) {
                View headerItem = inflater.inflate(R.layout.dates_calendar_header, null);

                ViewHolderHeader headerHolder = new ViewHolderHeader();
                headerHolder.weekday = headerItem.findViewById(R.id.weekday_short);
                headerItem.setTag(headerHolder);

                headerHolder.weekday.setText(new ArrayList<>(Arrays.asList(headerItem.getResources().getStringArray(R.array.weekdays))).get(i).substring(0, 2));

                LinearLayout ll = headerItem.findViewById(R.id.header_item);
                columSize = container.getMeasuredWidth() / 7 + 1;
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(columSize, ViewGroup.LayoutParams.WRAP_CONTENT);
                ll.setLayoutParams(params);

                mActivity.runOnUiThread(() -> header.addView(headerItem));
            }

            // After creading the header cread the grid...
            createGrid(inflater, root, container);
        }).start();
    }

    private void createGrid(LayoutInflater inflater, View root, ViewGroup container){
        new Thread(() -> {
            LinearLayout grid = root.findViewById(R.id.dates_calendar_grid);

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
        }).start();
    }

    private View createGridItem(int position, LayoutInflater layoutinflater, ViewGroup parent, int height){
        // Create the view...
        ViewHolder listViewHolder;
        listViewHolder = new ViewHolder();
        View convertView = layoutinflater.inflate(R.layout.dates_calendar_item, parent, false);
        listViewHolder.numberOfDateInListView = convertView.findViewById(R.id.number_of_date);
        listViewHolder.listInListView = convertView.findViewById(R.id.events_of_date);
        convertView.setTag(listViewHolder);

        LinearLayout ll = convertView.findViewById(R.id.dates_calendar_item_layout);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(columSize, height);
        ll.setLayoutParams(params);

        Day day = days.get(position);

        if (month != day.month) {
            listViewHolder.numberOfDateInListView.setBackgroundColor(convertView.getResources().getColor(R.color.calendarItemOtherMonth));
            listViewHolder.listInListView.setBackgroundColor(convertView.getResources().getColor(R.color.calendarItemOtherMonth));
        }
        listViewHolder.numberOfDateInListView.setText(Integer.toString(day.day));

        // Get day off week...
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day.day);
        calendar.set(Calendar.MONTH, day.month);
        calendar.set(Calendar.YEAR, day.year);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (weekday == -1 || weekday == 5)
            listViewHolder.numberOfDateInListView.setTextColor(convertView.getResources().getColor(R.color.calendarWeekend));

        // Get the current lesson...
        List<Event> events = day.getEvents();

        for (Event event : events) {

            @SuppressLint("InflateParams") View v = LayoutInflater.from(mActivity).inflate(R.layout.dates_calendar_event, null);
            TextView nameView = v.findViewById(R.id.event_name);

            nameView.setText(event.name);
            mActivity.runOnUiThread(() -> listViewHolder.listInListView.addView(v));
        }

        return convertView;
    }

    static class ViewHolderHeader {
        TextView weekday;
    }

    static class ViewHolder {
        LinearLayout listInListView;
        TextView numberOfDateInListView;
    }
}
