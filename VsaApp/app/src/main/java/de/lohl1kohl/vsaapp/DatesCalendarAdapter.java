package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.lohl1kohl.vsaapp.holder.DatesHolder;

public class DatesCalendarAdapter extends FragmentStatePagerAdapter {

    private int currentMonth;
    private int currentYear;
    private Context context;
    private List<DatesCalendarMonthFragment> monthsFragments = new ArrayList<>();
    private int currentItem = 0;

    DatesCalendarAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;

        java.util.Date date = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);

        setFragments();
    }

    public int setCurrentItem(int item){
        if (currentItem != item){
            currentItem = item;
            if (item == 0) {
                DatesCalendarMonthFragment fragment = new DatesCalendarMonthFragment();
                fragment.month = monthsFragments.get(0).month - 1;
                fragment.year = monthsFragments.get(0).year;
                if (fragment.month - 1 < 0) {
                    fragment.month = 11;
                    fragment.year--;
                }
                monthsFragments.add(0, fragment);
                notifyDataSetChanged();
                return -1;
            } else if (item == monthsFragments.size() - 1){
                DatesCalendarMonthFragment fragment = new DatesCalendarMonthFragment();
                fragment.month = monthsFragments.get(monthsFragments.size() - 1).month + 1;
                fragment.year = monthsFragments.get(monthsFragments.size() - 1).year;
                if (fragment.month > 11) {
                    fragment.month = 0;
                    fragment.year++;
                }
                monthsFragments.add(fragment);
                notifyDataSetChanged();
                return 1;
            }
        }
        return 0;
    }

    private void setFragments(){
        DatesCalendarMonthFragment fragment = new DatesCalendarMonthFragment();
        fragment.month = currentMonth - 1;
        fragment.year = currentYear;
        if (currentMonth - 1 < 0) {
            fragment.month = 11;
            fragment.year = currentYear - 1;
        }
        monthsFragments.add(fragment);

        fragment = new DatesCalendarMonthFragment();
        fragment.month = currentMonth;
        fragment.year = currentYear;
        monthsFragments.add(fragment);

        fragment = new DatesCalendarMonthFragment();
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
