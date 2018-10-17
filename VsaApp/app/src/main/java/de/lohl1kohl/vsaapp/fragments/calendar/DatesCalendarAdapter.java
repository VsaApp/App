package de.lohl1kohl.vsaapp.fragments.calendar;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.TextView;
import de.lohl1kohl.vsaapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DatesCalendarAdapter extends FragmentStatePagerAdapter {

    private int currentMonth;
    private int currentYear;
    private Context context;
    private List<DatesCalendarMonthFragment> monthsFragments = new ArrayList<>();
    private int currentItem = 0;
    private TextView monthName;
    private DatesFragment mFragment;

    DatesCalendarAdapter(Context context, DatesFragment mFragment, FragmentManager fm, TextView monthName) {
        super(fm);
        this.context = context;
        this.monthName = monthName;
        this.mFragment = mFragment;

        java.util.Date date = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);

        setFragments();
        setCurrentItem(-1);
    }

    public void update() {
        for (int i = 0; i < monthsFragments.size(); i++) {
            monthsFragments.get(i).update();
        }
    }

    public void setCurrentItem(int item) {
        if (currentItem != item) {
            currentItem = item;

            if (item == monthsFragments.size() - 1) {
                DatesCalendarMonthFragment fragment = new DatesCalendarMonthFragment();
                fragment.mFragment = mFragment;
                fragment.month = monthsFragments.get(monthsFragments.size() - 1).month + 1;
                fragment.year = monthsFragments.get(monthsFragments.size() - 1).year;
                if (fragment.month > 11) {
                    fragment.month = 0;
                    fragment.year++;
                }
                monthsFragments.add(fragment);
                notifyDataSetChanged();
            }
            DatesCalendarMonthFragment currentFragment = monthsFragments.get((currentItem == -1) ? 0 : currentItem);
            monthName.setText(String.format("%d, %s", currentFragment.year, Arrays.asList(context.getResources().getStringArray(R.array.monthNames)).get(currentFragment.month)));
        }
    }

    private void setFragments() {
        DatesCalendarMonthFragment fragment = new DatesCalendarMonthFragment();
        fragment.mFragment = mFragment;
        fragment.month = currentMonth;
        fragment.year = currentYear;
        monthsFragments.add(fragment);

        fragment = new DatesCalendarMonthFragment();
        fragment.mFragment = mFragment;
        fragment.month = currentMonth + 1;
        fragment.year = currentYear;
        if (currentMonth + 1 > 11) {
            fragment.month = 0;
            fragment.year = currentYear + 1;
        }
        monthsFragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return monthsFragments.get(position);
    }

    @Override
    public int getCount() {
        return monthsFragments.size();
    }
}
