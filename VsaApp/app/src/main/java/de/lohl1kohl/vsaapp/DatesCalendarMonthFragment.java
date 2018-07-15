package de.lohl1kohl.vsaapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class DatesCalendarMonthFragment extends BaseFragment {

    int month;
    int year;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dates_calendar_month, container, false);

        GridView header = root.findViewById(R.id.dates_calender_grid_header);
        DatesCalendarMonthHeaderAdapter headerAdapter = new DatesCalendarMonthHeaderAdapter(mActivity);
        header.setAdapter(headerAdapter);

        GridView gridView = root.findViewById(R.id.dates_calender_grid);
        DatesCalendarMonthAdapter adapter = new DatesCalendarMonthAdapter(mActivity, month, year);
        gridView.setAdapter(adapter);

        return root;
    }
}
