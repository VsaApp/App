package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class VpAdapter extends BaseAdapter {

    private ArrayList<Lesson> listStorage;
    private LayoutInflater layoutinflater;

    VpAdapter(Context context, ArrayList<Lesson> lessonList) {
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = lessonList;
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    public Lesson getLesson(int position) {
        return listStorage.get(position);
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
            convertView = layoutinflater.inflate(R.layout.vp_line, parent, false);
            listViewHolder.lessonInListView = convertView.findViewById(R.id.vp_lesson);
            listViewHolder.normalInListView = convertView.findViewById(R.id.vp_normal);
            listViewHolder.changesInListView = convertView.findViewById(R.id.vp_changes);
            convertView.setTag(listViewHolder);
        } else {
            listViewHolder = (ViewHolder) convertView.getTag();
        }
        Lesson nLesson = listStorage.get(position);
        Lesson cLesson = listStorage.get(position).changes;

        String normal = String.format("%s im Raum %s", nLesson.getName(), nLesson.room);
        String changes = String.format("%s", cLesson.name);

        if (cLesson.room.length() > 0)
            changes = String.format("%s (%s)", cLesson.name, cLesson.room);

        listViewHolder.lessonInListView.setText(nLesson.unit + ".");
        listViewHolder.normalInListView.setText(normal);
        listViewHolder.changesInListView.setText(changes);

        return convertView;
    }

    static class ViewHolder {
        TextView lessonInListView;
        TextView normalInListView;
        TextView changesInListView;
    }

}
