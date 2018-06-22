package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.TeacherHolder;

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
        listViewHolder.teacherInListView = convertView.findViewById(R.id.sp_teacher);
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
                if (event.getAction() == MotionEvent.ACTION_UP) {
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

        Subject subject = lesson.getSubject();
        String normalTeacher = subject.teacher;
        if (normalTeacher.length() > 0) {
            try {
                normalTeacher = Objects.requireNonNull(TeacherHolder.searchTeacher(normalTeacher)).getGenderizedGenitiveName();
            } catch (Exception ignored) {

            }
        }

        // Set the TextViews...
        if (lesson.numberOfSubjects() == 0) {
            listViewHolder.lessonInListView.setText("");
            if (position == 5)
                listViewHolder.teacherInListView.setText(convertView.getResources().getString(R.string.lesson_pause));
            else
                listViewHolder.teacherInListView.setText(convertView.getResources().getString(R.string.no_unit_in_this_lesson));
            listViewHolder.roomInListView.setText("");
        } else if (lesson.getSubject().name.equals(convertView.getResources().getString(R.string.lesson_free))) {
            listViewHolder.lessonInListView.setText("");
            listViewHolder.teacherInListView.setText(lesson.getSubject().name);
            listViewHolder.roomInListView.setText("");
        } else {
            listViewHolder.lessonInListView.setText(subject.getName());
            listViewHolder.teacherInListView.setText(String.format(convertView.getResources().getString(R.string.with_s), normalTeacher));
            listViewHolder.roomInListView.setText(String.format(convertView.getResources().getString(R.string.in_room_s), subject.room));
        }

        if (lesson.isGray()) {
            listViewHolder.lessonInListView.setTextColor(convertView.getResources().getColor(R.color.spPassedLesson));
            listViewHolder.teacherInListView.setTextColor(convertView.getResources().getColor(R.color.spPassedLesson));
            listViewHolder.roomInListView.setTextColor(convertView.getResources().getColor(R.color.spPassedLesson));
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean showVpinSp = sharedPref.getBoolean("pref_showVpinSp", true);

        if (showVpinSp) {
            Subject changes = lesson.getSubject().changes;
            if (changes != null) {
                String changedTeacher = changes.teacher;
                if (changedTeacher.length() > 0) {
                    try {
                        changedTeacher = Objects.requireNonNull(TeacherHolder.searchTeacher(changedTeacher)).getGenderizedGenitiveName();
                    } catch (Exception ignored) {

                    }
                }
                if (!changedTeacher.equals(normalTeacher) && !changedTeacher.equals("")) {
                    listViewHolder.teacherInListView.setText(String.format(convertView.getResources().getString(R.string.with_s), changedTeacher));
                    listViewHolder.teacherInListView.setTextColor(convertView.getResources().getColor(R.color.spChangeSubject));
                }
                if (!subject.getName().equals(changes.getName()) && !changes.getName().equals("")) {
                    listViewHolder.lessonInListView.setText(listViewHolder.lessonInListView.getText() + "\n" + changes.getName());
                    listViewHolder.lessonInListView.setTextColor(convertView.getResources().getColor(R.color.spChangeSubject));
                }
                if (!subject.room.equals(changes.room) && !changes.room.equals("")) {
                    listViewHolder.roomInListView.setText(String.format(convertView.getResources().getString(R.string.in_room_s), changes.room));
                    listViewHolder.roomInListView.setTextColor(convertView.getResources().getColor(R.color.spChangeSubject));
                }
            }
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView leftButton;
        TextView lessonInListView;
        TextView teacherInListView;
        TextView roomInListView;
        ImageView rightButton;
        RelativeLayout relativeLayout;
    }

}
