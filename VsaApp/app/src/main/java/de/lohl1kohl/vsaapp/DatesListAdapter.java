package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.TeacherHolder;

public class DatesListAdapter extends BaseAdapter {

    private List<List<Event>> listStorage;
    private LayoutInflater layoutinflater;
    private Context context;

    DatesListAdapter(Context context, List<List<Event>> eventList) {
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = eventList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listStorage.size();
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
        convertView = layoutinflater.inflate(R.layout.dates_list_item, parent, false);
        listViewHolder.dateInListView = convertView.findViewById(R.id.event_date);
        listViewHolder.listInListView = convertView.findViewById(R.id.event_events);
        convertView.setTag(listViewHolder);

        // Get the current lesson...
        List<Event> events = listStorage.get(position);

        for (Event event : events) {
            listViewHolder.dateInListView.setText(String.format("%s.%s.%s", event.start.getDay(), event.start.getMonth(context), event.start.getYear()));

            @SuppressLint("InflateParams") View v = LayoutInflater.from(context).inflate(R.layout.dates_list_item_event, null);
            TextView nameView = (TextView) v.findViewById(R.id.event_name);
            TextView timeView = (TextView) v.findViewById(R.id.event_time);

            nameView.setText(event.name);

            if (event.start != event.end)
                if (event.start.getHour() != 0) timeView.setText(String.format(context.getResources().getString(R.string.clock), event.start.getHour(), event.end.getHour()));
                else timeView.setText(String.format(context.getResources().getString(R.string.to_day), event.end.getDay(), event.end.getMonth(context), event.end.getYear()));
            else
                timeView.setText(context.getResources().getString(R.string.whole_day));

            listViewHolder.listInListView.addView(v);
        }

        return convertView;
    }

    static class ViewHolder {
        LinearLayout listInListView;
        TextView dateInListView;
    }

}
