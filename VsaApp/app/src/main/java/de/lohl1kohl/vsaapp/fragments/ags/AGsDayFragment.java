package de.lohl1kohl.vsaapp.fragments.ags;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.fragments.cafetoria.CafetoriaHolder.Day;
import de.lohl1kohl.vsaapp.fragments.cafetoria.CafetoriaHolder.Menu;

public class AGsDayFragment extends BaseFragment {

    int day;

    public void setDay(int day) {
        this.day = day;
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams", "SetTextI18n"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ags_day, container, false);

        new Thread(() -> {
            try {
                List<AGsHolder.AG> ags = AGsHolder.getDay(day);
                LinearLayout ll = root.findViewById(R.id.ags_day);

                for (AGsHolder.AG ag : ags) {
                    mActivity.runOnUiThread(() -> ll.addView(createView(inflater, ag)));
                }

            } catch (IndexOutOfBoundsException ignored) {

            }
        }).start();


        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(LayoutInflater layoutInflater, AGsHolder.AG ag) {
        // Create the view...
        ViewHolder topicHolder = new ViewHolder();
        View convertView = layoutInflater.inflate(R.layout.ag_cell, null);
        topicHolder.grade = convertView.findViewById(R.id.ag_cell_grade);
        topicHolder.room = convertView.findViewById(R.id.ag_cell_room);
        topicHolder.name = convertView.findViewById(R.id.ag_cell_name);
        topicHolder.time = convertView.findViewById(R.id.ag_cell_time);
        convertView.setTag(topicHolder);

        topicHolder.name.setText(ag.name);
        topicHolder.time.setText(ag.time);
        topicHolder.grade.setText(String.format(getString(R.string.ag_grade), ag.grades));
        topicHolder.room.setText(String.format(getString(R.string.ag_room), ag.room));

        return convertView;
    }

    static class ViewHolder {
        TextView grade;
        TextView room;
        TextView name;
        TextView time;
    }

}
