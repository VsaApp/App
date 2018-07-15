package de.lohl1kohl.vsaapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.SpHolder;


public class SpFragment extends BaseFragment {
    private View spView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        spView = inflater.inflate(R.layout.fragment_sp, container, false);

        // Try to refresh the sp...
        new Thread(this::syncSp).start();

        return spView;
    }

    public void syncSp() {
        // Get gradename...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");

        // Check if a gradename is set...
        if (grade.equals("-1")) {
            return;
        }
        fillSp();
    }

    public void fillSp() {
        mActivity.runOnUiThread(() -> {
            ViewPager pager = spView.findViewById(R.id.sp_viewpager);
            SpDayAdapter adapter = new SpDayAdapter(mActivity, getFragmentManager());
            pager.setAdapter(adapter);
            TabLayout tabLayout = spView.findViewById(R.id.sp_tabs);
            tabLayout.setupWithViewPager(pager);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            try {
                if (weekday == -1 | weekday == 5) weekday = 0;
                else if (LessonUtils.isLessonPassed(SpHolder.getNumberOfLessons(weekday) - 1))
                    weekday++;
                TabLayout.Tab tab = tabLayout.getTabAt(weekday);
                try {
                    Objects.requireNonNull(tab).select();
                } catch (Exception ignored) {
                }
            } catch (IndexOutOfBoundsException ignored) {

            }
        });
    }
}