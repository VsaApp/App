package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import de.lohl1kohl.vsaapp.holder.DatesHolder;

public class DatesCalendarMonthAdapter extends BaseAdapter {

    private int month;
    private List<Day> days;
    private LayoutInflater layoutinflater;
    private Context context;

    DatesCalendarMonthAdapter(Context context, int month, int year) {
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.month = month;

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

    @Override
    public int getCount() {
        return 42;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"SetTextI18n", "ViewHolder", "ClickableViewAccessibility"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Create the view...
        ViewHolder listViewHolder;
        listViewHolder = new ViewHolder();
        convertView = layoutinflater.inflate(R.layout.dates_calendar_item, parent, false);
        listViewHolder.numberOfDateInListView = convertView.findViewById(R.id.number_of_date);
        listViewHolder.listInListView = convertView.findViewById(R.id.events_of_date);
        convertView.setTag(listViewHolder);

        Day day = days.get(position);

        if (month != day.month)
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.calendarItemOtherMonth));
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

            @SuppressLint("InflateParams") View v = LayoutInflater.from(context).inflate(R.layout.dates_calendar_event, null);
            TextView nameView = v.findViewById(R.id.event_name);

            nameView.setText(event.name);
            listViewHolder.listInListView.addView(v);
        }

        return convertView;
    }

    static class ViewHolder {
        LinearLayout listInListView;
        TextView numberOfDateInListView;
    }

}
