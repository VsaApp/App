package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Arrays;

public class DatesCalendarMonthFragment extends BaseFragment {

    int month;
    int year;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dates_calendar_month, container, false);

        TextView monthYear = root.findViewById(R.id.dates_calendar_month_year);
        monthYear.setText(Arrays.asList(mActivity.getResources().getStringArray(R.array.monthNames)).get(month) + " " + year);

        GridView header = root.findViewById(R.id.dates_calendar_grid_header);
        DatesCalendarMonthHeaderAdapter headerAdapter = new DatesCalendarMonthHeaderAdapter(mActivity);
        header.setAdapter(headerAdapter);

        GridView gridView = root.findViewById(R.id.dates_calendar_grid);
        DatesCalendarMonthAdapter adapter = new DatesCalendarMonthAdapter(mActivity, month, year);
        gridView.setAdapter(adapter);

        return root;
    }
}
