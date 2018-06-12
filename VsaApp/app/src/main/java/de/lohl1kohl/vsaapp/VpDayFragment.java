package de.lohl1kohl.vsaapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

public class VpDayFragment extends Fragment {

    private List<Subject> data;
    private String weekday;
    private String date;
    private String time;
    private boolean hasInfo = false;

    public void setData(List<Subject> data) {
        this.data = data;
    }

    public void setInfo(String weekday, String date, String time) {
        this.weekday = weekday;
        this.date = date;
        this.time = time;
        this.hasInfo = true;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.vp_day, container, false);
        ListView listView = root.findViewById(R.id.vpList);
        VpAdapter vpAdapter = new VpAdapter(Objects.requireNonNull(getActivity()).getApplicationContext(), data);
        listView.setAdapter(vpAdapter);

        // Add click listener...
        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            Subject clickedLesson = vpAdapter.getSubject(position);
            VpFragment.showVpInfoDialog(getActivity(), clickedLesson);
        });
        if (this.hasInfo) {
            TextView textView = root.findViewById(R.id.vpStand);
            textView.setText(String.format(getString(R.string.for_s_the_s_from_s), weekday, date, time));
        }

        return root;
    }
}