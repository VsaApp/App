package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VpAdapter extends BaseAdapter {

    private List<Unit> listStorage;
    private LayoutInflater layoutinflater;

    VpAdapter(Context context, List<Unit> unitList) {
        layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = unitList;
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
        Unit nUnit = getUnit(position);
        Unit cUnit = getUnit(position).changes;

        String tutorNow = cUnit.tutor;

        String normal = String.format(convertView.getResources().getString(R.string.s_in_room_s), nUnit.getName(), nUnit.room);
        List<String> shortNames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(convertView.getResources().getStringArray(R.array.short_names))));
        List<String> longNames = new ArrayList<>(Arrays.asList(convertView.getResources().getStringArray(R.array.long_names)));

        if (tutorNow.length() > 0) {
            if (shortNames.contains(tutorNow)) {
                tutorNow = longNames.get(shortNames.indexOf(tutorNow));
                tutorNow = tutorNow.replace(convertView.getResources().getString(R.string.mister), convertView.getResources().getString(R.string.mister_gen));
            }
        }
        String changes = String.format("%s %s", tutorNow, cUnit.name);

        if (cUnit.room.length() > 0)
            changes = String.format("%s %s (%s)", tutorNow, cUnit.name, cUnit.room);

        listViewHolder.lessonInListView.setText(nUnit.unit + ".");
        listViewHolder.normalInListView.setText(normal);
        listViewHolder.normalInListView.setPaintFlags(listViewHolder.normalInListView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        listViewHolder.changesInListView.setText(changes);

        return convertView;
    }

    static class ViewHolder {
        TextView lessonInListView;
        TextView normalInListView;
        TextView changesInListView;
    }

}
