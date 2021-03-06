package de.lohl1kohl.vsaapp.fragments.sp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.holders.SpHolder;
import de.lohl1kohl.vsaapp.holders.TeacherHolder;
import de.lohl1kohl.vsaapp.holders.VpHolder;

import java.util.ArrayList;
import java.util.List;

public class SpDayFragment extends BaseFragment {

    int day;
    int lineHeight;
    int lineWidth;

    public void setDay(int day) {
        this.day = day;
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams", "SetTextI18n"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sp_day, container, false);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Rect r = new Rect();
                root.getWindowVisibleDisplayFrame(r);
                int screenHeight = root.getRootView().getHeight();
                int heightDifference = screenHeight - (r.bottom - r.top);

                new Thread(() -> {
                    if (container != null) {
                        lineHeight = container.getMeasuredHeight() / 5;
                        lineWidth = container.getMeasuredWidth();
                    }
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isAcceptingText()) {
                        lineHeight += heightDifference;
                    }

                    try {
                        // Get preferences...
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        List<Lesson> spDay;
                        if (sharedPref.getBoolean("pref_lockSubjects", false)) spDay = SpHolder.getDay(day);
                        else spDay = SpHolder.getUntrimmedDay(day);
                        LinearLayout ll = root.findViewById(R.id.sp_day);

                        LayoutInflater layoutinflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        List<View> views = new ArrayList<>();

                        for (int position = 0; position < spDay.size(); position++) {
                            View view = createView(layoutinflater, spDay, position, sharedPref);
                            views.add(view);
                            mActivity.runOnUiThread(() -> ll.addView(view));
                        }

                        new Thread(() -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int i = 0; i < views.size(); i++) {
                                RelativeLayout rL = views.get(i).findViewById(R.id.sp_rl);
                                int currentHeight = rL.getMeasuredHeight();
                                if (lineHeight != 0 && currentHeight < lineHeight) {
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(lineWidth, lineHeight);
                                    mActivity.runOnUiThread(() -> rL.setLayoutParams(params));
                                }
                            }
                        }).start();

                    } catch (IndexOutOfBoundsException ignored) {

                    }
                }).start();
            }
        });

        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(LayoutInflater layoutInflater, List<Lesson> spDay, int position, SharedPreferences sharedPref) {
        // Create the view...
        ViewHolder listViewHolder = new ViewHolder();
        View convertView = layoutInflater.inflate(R.layout.sp_cell, null);
        listViewHolder.leftButton = convertView.findViewById(R.id.sp_left);
        listViewHolder.lessonInListView = convertView.findViewById(R.id.sp_lesson);
        listViewHolder.teacherInListView = convertView.findViewById(R.id.sp_teacher);
        listViewHolder.roomInListView = convertView.findViewById(R.id.sp_room);
        listViewHolder.rightButton = convertView.findViewById(R.id.sp_right);
        listViewHolder.relativeLayout = convertView.findViewById(R.id.sp_rl);
        convertView.setTag(listViewHolder);

        Boolean lock = sharedPref.getBoolean("pref_lockSubjects", false);

        // Get the current lesson...
        Lesson lesson = spDay.get(position);

        // Set the buttons...
        if (lesson.numberOfSubjects() <= 1 || lock) {
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
                        Lesson clickedLesson = spDay.get(position);
                        clickedLesson.setSubject(update);
                        clickedLesson.saveSubject(mActivity);
                        Subject subject = clickedLesson.getSubject();

                        // Update vp...
                        VpHolder.updateVpList(mActivity);

                        View newView = createView(layoutInflater, spDay, position, sharedPref);
                        ViewGroup parent = (ViewGroup) convertView.getParent();
                        int i = parent.indexOfChild(convertView);
                        parent.removeView(convertView);
                        parent.addView(newView, i);
                    }
                }
                return true;
            });
        }
        String normalTeacher = "";
        Subject subject = null;
        try {
            subject = lesson.getSubject();
            normalTeacher = subject.teacher;
            if (normalTeacher.length() > 0) {
                try {
                    normalTeacher = TeacherHolder.searchTeacher(normalTeacher).getGenderizedGenitiveName();
                } catch (Exception ignored) {

                }
            }
        } catch (IndexOutOfBoundsException ignored) {

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
        } else if (lesson.getSubject().name.equals(convertView.getResources().getString(R.string.lesson_tandem))) {
            listViewHolder.lessonInListView.setText(lesson.getSubject().name);
            listViewHolder.teacherInListView.setText(lesson.getSubject().room);
            listViewHolder.roomInListView.setText(lesson.getSubject().teacher);
        } else {
            listViewHolder.lessonInListView.setText(subject.getName());
            listViewHolder.teacherInListView.setText(String.format(convertView.getResources().getString(R.string.with_s), normalTeacher));
            listViewHolder.roomInListView.setText(String.format(convertView.getResources().getString(R.string.in_room_s), subject.room));
        }

        if (lesson.isGray()) {
            listViewHolder.lessonInListView.setTextColor(getColor(convertView, false, true));
            listViewHolder.teacherInListView.setTextColor(getColor(convertView, false, true));
            listViewHolder.roomInListView.setTextColor(getColor(convertView, false, true));
        }

        Boolean showVpinSp = sharedPref.getBoolean("pref_showVpinSp", true);

        if (lesson.numberOfSubjects() > 0) {
            if (showVpinSp) {
                Subject changes = lesson.getSubject().changes;
                if (changes != null) {
                    String changedTeacher = changes.teacher;
                    if (changedTeacher.length() > 0) {
                        try {
                            changedTeacher = TeacherHolder.searchTeacher(changedTeacher).getGenderizedGenitiveName();
                        } catch (Exception ignored) {

                        }
                    }
                    if (!changedTeacher.equals(normalTeacher) && !changedTeacher.equals("")) {
                        listViewHolder.teacherInListView.setText(String.format(convertView.getResources().getString(R.string.with_s), changedTeacher));
                        listViewHolder.teacherInListView.setTextColor(getColor(convertView, true, lesson.isGray()));
                    }
                    if (!subject.getName().equals(changes.getName()) && !changes.getName().equals("")) {
                        listViewHolder.lessonInListView.setText(listViewHolder.lessonInListView.getText() + "\n" + changes.getName());
                        listViewHolder.lessonInListView.setTextColor(getColor(convertView, true, lesson.isGray()));
                    }
                    if (!subject.room.equals(changes.room) && !changes.room.equals("")) {
                        listViewHolder.roomInListView.setText(String.format(convertView.getResources().getString(R.string.in_room_s), changes.room));
                        listViewHolder.roomInListView.setTextColor(getColor(convertView, true, lesson.isGray()));
                    }
                }
                //TODO: Not tested yet...
                else if (lesson.getSubject().name.equals(convertView.getResources().getString(R.string.lesson_tandem))) {
                    for (int i = 0; i < lesson.numberOfSubjects(); i++) {
                        if (lesson.getSubject(i).changes != null) {
                            if (lesson.getSubject(i).getName().equals(convertView.getResources().getString(R.string.lesson_french))) {
                                listViewHolder.teacherInListView.setTextColor(getColor(convertView, true, lesson.isGray()));
                            } else {
                                listViewHolder.roomInListView.setTextColor(getColor(convertView, true, lesson.isGray()));
                            }
                        }
                    }
                }
            }
        }

        new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                View newView = createView(layoutInflater, spDay, position, sharedPref);
                ViewGroup parent = (ViewGroup) convertView.getParent();
                int i = parent.indexOfChild(convertView);
                mActivity.runOnUiThread(() -> {
                    parent.removeView(convertView);
                    parent.addView(newView, i);
                });

                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    RelativeLayout rL = newView.findViewById(R.id.sp_rl);
                    int currentHeight = rL.getMeasuredHeight();
                    if (lineHeight != 0 && currentHeight < lineHeight) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(lineWidth, lineHeight);
                        mActivity.runOnUiThread(() -> rL.setLayoutParams(params));
                    }
                }).start();
            } catch (NullPointerException ignored) {

            }
        }).start();
        return convertView;
    }

    private int getColor(View view, boolean changed, boolean isGray) {
        if (changed) {
            if (isGray) return view.getResources().getColor(R.color.spChangePassedSubject);
            else return view.getResources().getColor(R.color.spChangeSubject);

        } else {
            if (isGray) return view.getResources().getColor(R.color.spPassedLesson);
            else return view.getResources().getColor(R.color.spLesson);
        }
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