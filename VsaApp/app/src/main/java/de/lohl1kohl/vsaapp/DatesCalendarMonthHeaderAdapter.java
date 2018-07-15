package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class DatesCalendarMonthHeaderAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private Context context;

    DatesCalendarMonthHeaderAdapter(Context context) {
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getCount() {
        return 7;
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

        ViewHolder headerHolder = new ViewHolder();
        convertView = layoutinflater.inflate(R.layout.dates_calendar_header, parent, false);
        headerHolder.weekday = convertView.findViewById(R.id.weekday_short);
        convertView.setTag(headerHolder);

        headerHolder.weekday.setText(new ArrayList<>(Arrays.asList(convertView.getResources().getStringArray(R.array.weekdays))).get(position).substring(0, 2));

        return convertView;
    }

    static class ViewHolder {
        TextView weekday;
    }

}
