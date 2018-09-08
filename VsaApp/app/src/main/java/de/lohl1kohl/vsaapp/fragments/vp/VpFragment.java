package de.lohl1kohl.vsaapp.fragments.vp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.fragments.sp.Subject;
import de.lohl1kohl.vsaapp.fragments.teachers.TeacherHolder;


public class VpFragment extends BaseFragment {
    @SuppressLint("StaticFieldLeak")
    static View vpView;
    static String day;

    @SuppressLint("SetTextI18n")
    static void showVpInfoDialog(Context context, Subject subject) {
        final Dialog loginDialog = new Dialog(context);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(loginDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_vp_info);
        loginDialog.setCancelable(true);
        loginDialog.setTitle(R.string.vpInfoDialogTitle);

        String teacherNormal = subject.teacher;
        String teacherNow = subject.changes.teacher;

        if (teacherNormal.length() > 0) {
            try {
                teacherNormal = TeacherHolder.searchTeacher(teacherNormal).getGenderizedGenitiveName();
            } catch (Exception ignored) {

            }
        }

        if (teacherNow.length() > 0) {
            try {
                teacherNow = TeacherHolder.searchTeacher(teacherNow).getGenderizedGenitiveName();
            } catch (Exception ignored) {

            }
        }

        // Get all TextViews...
        final TextView tV_units = loginDialog.findViewById(R.id.lbl_unit);
        final TextView tV_normal = loginDialog.findViewById(R.id.lbl_normal);
        final TextView tV_changed = loginDialog.findViewById(R.id.lbl_changed);


        tV_units.setText(Integer.toString(subject.unit + 1) + context.getString(R.string.dot_unit));

        if (subject.room.equals("?")) tV_normal.setVisibility(TextView.GONE);
        else {
            tV_normal.setText(String.format(context.getString(R.string.with_s_s_in_room_s), teacherNormal, subject.getName(), subject.room));
            tV_normal.setPaintFlags(tV_normal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        String text;
        if (subject.changes.teacher.length() > 0)
            text = String.format(context.getString(R.string.now_with_s_s), teacherNow, subject.changes.name);
        else text = String.format(context.getString(R.string.now_s), subject.changes.getName());
        if (subject.changes.room.length() > 0)
            text += String.format(context.getString(R.string.in_room_s), subject.changes.room);
        tV_changed.setText(text);

        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }

    public static void selectDay(String day) {
        VpFragment.day = day;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vpView = inflater.inflate(R.layout.fragment_vp, container, false);
        // Load vp...
        fillVp();
        if (day != null) {
            TabLayout tabLayout = vpView.findViewById(R.id.vp_tabs);
            if (day.equals(VpHolder.weekdayTomorrow)) {
                tabLayout.getTabAt(1).select();
            }
        }
        return vpView;
    }

    private void fillVp() {
        ViewPager pager = vpView.findViewById(R.id.vp_viewpager);
        VpDayAdapter adapter = new VpDayAdapter(getFragmentManager());
        pager.setAdapter(adapter);

        // Add the tabs...
        TabLayout tabLayout = vpView.findViewById(R.id.vp_tabs);
        tabLayout.setupWithViewPager(pager);
        if (VpHolder.weekdayToday.equals(VpHolder.weekdayTomorrow)) {
            tabLayout.removeTabAt(1);
            adapter.fragments.remove(1);
            adapter.notifyDataSetChanged();
        }
    }
}