package de.lohl1kohl.vsaapp.fragments.vp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.fragments.sp.Subject;
import de.lohl1kohl.vsaapp.fragments.teachers.TeacherHolder;

public class VpDayFragment extends BaseFragment {

    public boolean today;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.vp_day, container, false);
        new Thread(() -> {
            LinearLayout ll = root.findViewById(R.id.vpList);
            LayoutInflater layoutinflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (int position = 0; position < VpHolder.getVp(today).size(); position++) {
                ViewHolder listViewHolder;
                listViewHolder = new ViewHolder();
                View convertView = layoutinflater.inflate(R.layout.vp_line, null);
                listViewHolder.lessonInListView = convertView.findViewById(R.id.vp_lesson);
                listViewHolder.normalInListView = convertView.findViewById(R.id.vp_normal);
                listViewHolder.changesInListView = convertView.findViewById(R.id.vp_changes);

                Subject nSubject = VpHolder.getSubject(today, position);
                Subject cSubject = nSubject.changes;

                String teacherNow = cSubject.teacher;

                String normal = String.format(convertView.getResources().getString(R.string.s_in_room_s), nSubject.getName(), nSubject.room);

                if (teacherNow.length() > 0) {
                    try {
                        teacherNow = TeacherHolder.searchTeacher(teacherNow).getGenderizedName();
                    } catch (Exception ignored) {

                    }
                }

                String changes = String.format("%s %s", teacherNow, cSubject.name);

                if (cSubject.room.length() > 0)
                    changes = String.format("%s %s (%s)", teacherNow, cSubject.name, cSubject.room);

                listViewHolder.lessonInListView.setText((nSubject.unit + 1) + ".");
                listViewHolder.normalInListView.setPaintFlags(listViewHolder.normalInListView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                listViewHolder.changesInListView.setText(changes);

                if (nSubject.room.equals("?"))
                    listViewHolder.normalInListView.setVisibility(TextView.GONE);
                else listViewHolder.normalInListView.setText(normal);

                int finalPosition = position;
                convertView.setOnClickListener(view -> {
                    Subject clickedLesson = VpHolder.getSubject(today, finalPosition);
                    VpFragment.showVpInfoDialog(mActivity, clickedLesson);
                });
                mActivity.runOnUiThread(() -> ll.addView(convertView));
            }

            TextView textView = root.findViewById(R.id.vpStand);
            if (today)
                mActivity.runOnUiThread(() -> textView.setText(String.format(getString(R.string.for_s_the_s_from_s), VpHolder.weekdayToday, VpHolder.dateToday, VpHolder.timeToday)));
            else
                mActivity.runOnUiThread(() -> textView.setText(String.format(getString(R.string.for_s_the_s_from_s), VpHolder.weekdayTomorrow, VpHolder.dateTomorrow, VpHolder.timeTomorrow)));

        }).start();

        return root;
    }

    static class ViewHolder {
        TextView lessonInListView;
        TextView normalInListView;
        TextView changesInListView;
    }
}