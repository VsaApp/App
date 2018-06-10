package de.lohl1kohl.vsaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SpAdapter extends BaseAdapter {

    public ArrayList<Lesson> listStorage = new ArrayList<Lesson>();
    private LayoutInflater layoutinflater;
    private Context context;

    public SpAdapter(Context context, ArrayList<Lesson> lessonList, Boolean noConverting) {
        this.context = context;
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = lessonList;
    }

    public SpAdapter(Context context, ArrayList<ArrayList<Lesson>> lessonList) {
        this.context = context;
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int maxLessonsPerDay = 5;

        for (ArrayList<Lesson> day : lessonList) {
            if (day.size() > maxLessonsPerDay) {
                maxLessonsPerDay = day.size();
            }
        }

        for (int lesson = 0; lesson < maxLessonsPerDay; lesson++) {
            for (int day = 0; day < 5; day++) {
                try {
                    listStorage.add(lessonList.get(day).get(lesson));
                } catch (Exception e) {
                    listStorage.add(null);
                }
            }
        }
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder listViewHolder;
        if (convertView == null) {
            listViewHolder = new ViewHolder();
            convertView = layoutinflater.inflate(R.layout.sp_cell, parent, false);
            listViewHolder.lessonInListView = (TextView) convertView.findViewById(R.id.sp_lesson);
            listViewHolder.roomInListView = (TextView) convertView.findViewById(R.id.sp_room);
            listViewHolder.teacherInListView = (TextView) convertView.findViewById(R.id.sp_tutor);
            convertView.setTag(listViewHolder);
        } else {
            listViewHolder = (ViewHolder) convertView.getTag();
        }

        if (listStorage.get(position) == null) {
            convertView.setVisibility(View.GONE);
            convertView.setClickable(false);
            convertView.setEnabled(false);
            return convertView;
        }

        // There is a subject in this lesson...
        if (listStorage.get(position).changed) {
            listViewHolder.lessonInListView.setTextColor(parent.getResources().getColor(R.color.spChangedColor));
            listViewHolder.roomInListView.setTextColor(parent.getResources().getColor(R.color.spChangedColor));
            listViewHolder.teacherInListView.setTextColor(parent.getResources().getColor(R.color.spChangedColor));
        }
        listViewHolder.lessonInListView.setText(listStorage.get(position).getName());
        listViewHolder.roomInListView.setText(listStorage.get(position).room);
        listViewHolder.teacherInListView.setText(listStorage.get(position).tutor);

        return convertView;
    }

    static class ViewHolder {
        TextView lessonInListView;
        TextView roomInListView;
        TextView teacherInListView;
    }

}
