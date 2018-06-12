package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SpDayAdapter extends BaseAdapter {

    private List<Lesson> listStorage;
    private LayoutInflater layoutinflater;
    private SpDayAdapter adapter;
    private Context context;

    SpDayAdapter(Context context, List<Lesson> lessonList) {
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = lessonList;
        adapter = this;
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

    public Unit getUnit(int position) {
        return listStorage.get(position).getUnit();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder listViewHolder;
        if (convertView == null) {
            listViewHolder = new ViewHolder();
            convertView = layoutinflater.inflate(R.layout.sp_cell, parent, false);
            listViewHolder.leftButton = convertView.findViewById(R.id.sp_left);
            listViewHolder.lessonInListView = convertView.findViewById(R.id.sp_lesson);
            listViewHolder.tutorInListView = convertView.findViewById(R.id.sp_tutor);
            listViewHolder.roomInListView = convertView.findViewById(R.id.sp_room);
            listViewHolder.rightButton = convertView.findViewById(R.id.sp_right);
            convertView.setTag(listViewHolder);
        } else {
            listViewHolder = (ViewHolder) convertView.getTag();
        }

        Lesson lesson = listStorage.get(position);
        Unit unit = lesson.getUnit();

        // If there is only one unit hide the buttons...
        if (lesson.numberOfUnits() == 1) {
            listViewHolder.leftButton.setEnabled(false);
            listViewHolder.leftButton.setVisibility(ImageButton.INVISIBLE);
            listViewHolder.rightButton.setEnabled(false);
            listViewHolder.rightButton.setVisibility(ImageButton.INVISIBLE);
        }
        else {
            listViewHolder.leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Lesson clickedLesson = listStorage.get(position);
                    clickedLesson.setUnit(-1);
                    clickedLesson.saveUnit(context);
                    adapter.notifyDataSetChanged();

                }
            });
            listViewHolder.rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Lesson clickedLesson = listStorage.get(position);
                    clickedLesson.setUnit(+1);
                    clickedLesson.saveUnit(context);
                    adapter.notifyDataSetChanged();
                }
            });
        }


        List<String> shortNames = new ArrayList<String>(Arrays.asList(Objects.requireNonNull(convertView.getResources().getStringArray(R.array.short_names))));
        List<String> longNames = new ArrayList<String>(Arrays.asList(convertView.getResources().getStringArray(R.array.long_names)));

        String tutor = unit.tutor;
        if (tutor.length() > 0) {
            if (shortNames.contains(tutor)) {
                tutor = longNames.get(shortNames.indexOf(tutor));
                tutor = tutor.replace(convertView.getResources().getString(R.string.mister), convertView.getResources().getString(R.string.mister_gen));
            }
        }

        listViewHolder.lessonInListView.setText(unit.getName());
        listViewHolder.tutorInListView.setText(String.format(convertView.getResources().getString(R.string.with_s), tutor));
        listViewHolder.roomInListView.setText(convertView.getResources().getString(R.string.in_room) + " " + unit.room);

        return convertView;
    }

    static class ViewHolder {
        ImageButton leftButton;
        TextView lessonInListView;
        TextView tutorInListView;
        TextView roomInListView;
        ImageButton rightButton;
    }

}
