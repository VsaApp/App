package de.lohl1kohl.vsaapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import de.lohl1kohl.vsaapp.holder.VpHolder;

public class VpDayFragment extends BaseFragment {

    public boolean today;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.vp_day, container, false);
        ListView listView = root.findViewById(R.id.vpList);
        VpAdapter vpAdapter = new VpAdapter(mActivity, today);
        listView.setAdapter(vpAdapter);

        // Add click listener...
        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            Subject clickedLesson = VpHolder.getSubject(today, position);
            VpFragment.showVpInfoDialog(mActivity, clickedLesson);
        });

        TextView textView = root.findViewById(R.id.vpStand);
        if (today)
            textView.setText(String.format(getString(R.string.for_s_the_s_from_s), VpHolder.weekdayToday, VpHolder.dateToday, VpHolder.timeToday));
        else
            textView.setText(String.format(getString(R.string.for_s_the_s_from_s), VpHolder.weekdayTomorrow, VpHolder.dateTomorrow, VpHolder.timeTomorrow));

        return root;
    }
}