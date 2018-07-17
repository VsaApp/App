package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.lohl1kohl.vsaapp.holder.AGsHolder;
import de.lohl1kohl.vsaapp.holder.Callbacks;


public class AGsFragment extends BaseFragment {
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout list;
    private LayoutInflater inflater;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View root = inflater.inflate(R.layout.fragment_ags, container, false);
        list = root.findViewById(R.id.agsList);
        Callbacks.agsLoadedCallback agsLoadedCallback = new Callbacks.agsLoadedCallback() {
            @Override
            public void onOldLoaded() {
                listAGs();
            }

            @Override
            public void onNewLoaded() {
                listAGs();
            }

            @Override
            public void onConnectionFailed() {
                mActivity.runOnUiThread(() -> Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_LONG).show());
            }
        };
        AGsHolder.load(mActivity, agsLoadedCallback);
        return root;
    }

    @SuppressLint("InflateParams")
    public void listAGs() {
        mActivity.runOnUiThread(list::removeAllViews);
        for (AGsHolder.AG ag : AGsHolder.getFilteredAGs(mActivity)) {
            View view = inflater.inflate(R.layout.ag_cell, null);
            TextView weekday = view.findViewById(R.id.ag_cell_weekday);
            TextView name = view.findViewById(R.id.ag_cell_name);
            TextView time = view.findViewById(R.id.ag_cell_time);
            TextView room = view.findViewById(R.id.ag_cell_room);
            TextView grades = view.findViewById(R.id.ag_cell_grades);
            weekday.setText(ag.weekday);
            name.setText(ag.name);
            time.setText(ag.time);
            room.setText(ag.room);
            grades.setText(ag.grades);
            mActivity.runOnUiThread(() -> list.addView(view));
        }
        mActivity.runOnUiThread(() -> {
            try {
                list.getChildAt(list.getChildCount() - 1).findViewById(R.id.line).setVisibility(View.GONE);
            } catch (NullPointerException ignored) {

            }
        });
    }
}