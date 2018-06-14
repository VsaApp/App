package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpDayListAdapter extends BaseAdapter {

    private List<Lesson> listStorage;
    private LayoutInflater layoutinflater;
    private SpDayListAdapter adapter;
    private Context context;

    SpDayListAdapter(Context context, List<Lesson> lessonList) {
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"SetTextI18n", "ViewHolder", "ClickableViewAccessibility"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        // Create the view...
        ViewHolder listViewHolder;
        listViewHolder = new ViewHolder();
        convertView = layoutinflater.inflate(R.layout.sp_cell, parent, false);
        listViewHolder.leftButton = convertView.findViewById(R.id.sp_left);
        listViewHolder.lessonInListView = convertView.findViewById(R.id.sp_lesson);
        listViewHolder.tutorInListView = convertView.findViewById(R.id.sp_tutor);
        listViewHolder.roomInListView = convertView.findViewById(R.id.sp_room);
        listViewHolder.rightButton = convertView.findViewById(R.id.sp_right);
        listViewHolder.relativeLayout = convertView.findViewById(R.id.sp_rl);
        convertView.setTag(listViewHolder);

        // Get the current lesson...
        Lesson lesson = listStorage.get(position);

        // Set the buttons...
        if (lesson.numberOfSubjects() <= 1) {
            listViewHolder.leftButton.setEnabled(false);
            listViewHolder.leftButton.setVisibility(ImageButton.INVISIBLE);
            listViewHolder.rightButton.setEnabled(false);
            listViewHolder.rightButton.setVisibility(ImageButton.INVISIBLE);

        } else if (lesson.numberOfSubjects() > 1) {
            listViewHolder.relativeLayout.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int update = 0;
                    if (event.getX() < listViewHolder.relativeLayout.getWidth() / 4) {
                        update = -1;
                    } else if (event.getX() > listViewHolder.relativeLayout.getWidth() / 4 * 3) {
                        update = 1;
                    }
                    if (update != 0) {
                        Lesson clickedLesson = listStorage.get(position);
                        clickedLesson.setSubject(update);
                        clickedLesson.saveSubject(context);
                        adapter.notifyDataSetChanged();
                    }
                }
                return true;
            });
        }

        // Set the TextViews...
        if (lesson.numberOfSubjects() == 0) {
            listViewHolder.lessonInListView.setText("");
            if (position == 5)
                listViewHolder.tutorInListView.setText(convertView.getResources().getString(R.string.lesson_pause));
            else
                listViewHolder.tutorInListView.setText(convertView.getResources().getString(R.string.no_unit_in_this_lesson));
            listViewHolder.roomInListView.setText("");
        } else if (lesson.getSubject().name.equals(convertView.getResources().getString(R.string.lesson_free))) {
            listViewHolder.lessonInListView.setText("");
            listViewHolder.tutorInListView.setText(lesson.getSubject().name);
            listViewHolder.roomInListView.setText("");
        } else {
            List<String> shortNames = new ArrayList<>(Arrays.asList(convertView.getResources().getStringArray(R.array.short_names)));
            List<String> longNames = new ArrayList<>(Arrays.asList(convertView.getResources().getStringArray(R.array.long_names)));

            Subject subject = lesson.getSubject();
            String tutor = subject.tutor;
            if (tutor.length() > 0) {
                if (shortNames.contains(tutor)) {
                    tutor = longNames.get(shortNames.indexOf(tutor));
                    tutor = tutor.replace(convertView.getResources().getString(R.string.mister), convertView.getResources().getString(R.string.mister_gen));
                }
            }

            listViewHolder.lessonInListView.setText(subject.getName());
            listViewHolder.tutorInListView.setText(String.format(convertView.getResources().getString(R.string.with_s), tutor));
            listViewHolder.roomInListView.setText(convertView.getResources().getString(R.string.in_room) + " " + subject.room);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView leftButton;
        TextView lessonInListView;
        TextView tutorInListView;
        TextView roomInListView;
        ImageView rightButton;
        RelativeLayout relativeLayout;
    }

}
