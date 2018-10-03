package de.lohl1kohl.vsaapp.fragments.calendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.TimeZone;
import java.util.zip.Inflater;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.holders.DatesHolder;

public class DatesListFragment extends BaseFragment {

    private View view;
    public DatesFragment parent;
    private LayoutInflater infalter;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dates_list, container, false);

        view = root;
        this.infalter = inflater;

        createView(root, inflater);

        return root;
    }

    public void update(){
        createView(view, infalter);
    }

    public void createView(View root, LayoutInflater inflater){
        new Thread(() -> {
            List<Day> calendar = DatesHolder.getFilteredCalendar(mActivity);
            LinearLayout ll = root.findViewById(R.id.datesList);

            mActivity.runOnUiThread(ll::removeAllViews);

            for (int position = 0; position < calendar.size(); position++) {
                // Create the view...
                ViewHolder listViewHolder = new ViewHolder();
                View convertView = inflater.inflate(R.layout.dates_list_item, null);
                listViewHolder.dateInListView = convertView.findViewById(R.id.event_date);
                listViewHolder.listInListView = convertView.findViewById(R.id.event_events);
                convertView.setTag(listViewHolder);

                // Get the current lesson...
                Day day = calendar.get(position);
                List<Event> events = day.getEvents();
                listViewHolder.dateInListView.setText(String.format("%s.%s.%s", day.day, day.month, day.year));

                for (Event event : events) {
                    View v = LayoutInflater.from(mActivity).inflate(R.layout.dates_list_item_event, null);
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
                        parent.editEvent(event);
                    });

                    listViewHolder.listInListView.addView(v);
                }
                mActivity.runOnUiThread(() -> ll.addView(convertView));
            }
        }).start();
    }

    static class ViewHolder {
        LinearLayout listInListView;
        TextView dateInListView;
    }

}
