package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.DatesHolder;

public class DatesListFragment extends BaseFragment {

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dates_list, container, false);

        LinearLayout ll = root.findViewById(R.id.datesList);
        LayoutInflater layoutinflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<Day> calendar = DatesHolder.getFilteredCalendar(mActivity);

        new Thread(() -> {
            for (int position = 0; position < calendar.size(); position++) {
                // Create the view...
                ViewHolder listViewHolder = new ViewHolder();
                View convertView = Objects.requireNonNull(layoutinflater).inflate(R.layout.dates_list_item, null);
                listViewHolder.dateInListView = convertView.findViewById(R.id.event_date);
                listViewHolder.listInListView = convertView.findViewById(R.id.event_events);
                convertView.setTag(listViewHolder);

                // Get the current lesson...
                Day day = calendar.get(position);
                List<Event> events = day.getEvents();
                listViewHolder.dateInListView.setText(String.format("%s.%s.%s", day.day, day.month, day.year));

                for (Event event : events) {
                    View v = LayoutInflater.from(mActivity).inflate(R.layout.dates_list_item_event, null);
                    TextView nameView = v.findViewById(R.id.list_event_name);
                    TextView timeView = v.findViewById(R.id.list_event_time);

                    nameView.setText(event.name);

                    if (event.start != event.end)
                        if (event.start.getHour() != 0)
                            timeView.setText(String.format(mActivity.getResources().getString(R.string.clock), event.start.getHour(), event.end.getHour()));
                        else
                            timeView.setText(String.format(mActivity.getResources().getString(R.string.to_day), event.end.getDay(), event.end.getMonth(mActivity), event.end.getYear()));
                    else
                        timeView.setText(mActivity.getResources().getString(R.string.whole_day));

                    listViewHolder.listInListView.addView(v);
                }
                mActivity.runOnUiThread(() -> ll.addView(convertView));
            }
        }).start();

        return root;
    }

    static class ViewHolder {
        LinearLayout listInListView;
        TextView dateInListView;
    }

}
