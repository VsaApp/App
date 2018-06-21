package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.lohl1kohl.vsaapp.holder.TeacherHolder;
import de.lohl1kohl.vsaapp.holder.VpHolder;

public class VpAdapter extends BaseAdapter {

    private boolean today;
    private LayoutInflater layoutinflater;

    VpAdapter(Context context, boolean today) {
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.today = today;
    }

    @Override
    public int getCount() {
        return VpHolder.getVp(today).size();
    }

    @Override
    public Object getItem(int position) {
        return position;
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

        Subject nSubject = VpHolder.getSubject(today, position);
        Subject cSubject = nSubject.changes;

        String teacherNow = cSubject.teacher;

        String normal = String.format(convertView.getResources().getString(R.string.s_in_room_s), nSubject.getName(), nSubject.room);

        if (teacherNow.length() > 0) {
            List<Teacher> possibleTeachers = TeacherHolder.searchTeacher(teacherNow);
            if (possibleTeachers.size() > 0) {
                teacherNow = possibleTeachers.get(0).getGenderizedGenitiveName();
            }
        }

        String changes = String.format("%s %s", teacherNow, cSubject.name);

        if (cSubject.room.length() > 0)
            changes = String.format("%s %s (%s)", teacherNow, cSubject.name, cSubject.room);

        listViewHolder.lessonInListView.setText((nSubject.unit + 1) + ".");
        listViewHolder.normalInListView.setPaintFlags(listViewHolder.normalInListView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        listViewHolder.changesInListView.setText(changes);

        if (nSubject.room.equals("?")) listViewHolder.normalInListView.setVisibility(TextView.GONE);
        else listViewHolder.normalInListView.setText(normal);

        return convertView;
    }

    static class ViewHolder {
        TextView lessonInListView;
        TextView normalInListView;
        TextView changesInListView;
    }

}
