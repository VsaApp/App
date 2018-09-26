package de.lohl1kohl.vsaapp.fragments.sp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.holders.SpHolder;


public class SpFragment extends BaseFragment {
    private View spView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        spView = inflater.inflate(R.layout.fragment_sp, container, false);
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
                tab.select();
            } catch (Exception ignored) {

            }
        } catch (IndexOutOfBoundsException ignored) {

        }
        return spView;
    }
}