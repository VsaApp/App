package de.lohl1kohl.vsaapp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import de.lohl1kohl.vsaapp.holder.VpHolder;

public class DatesFragmentAdapter extends FragmentStatePagerAdapter {

    private DatesListFragment datesListFragment = new DatesListFragment();
    private DatesCalenderFragment datesCalenderFragment = new DatesCalenderFragment();
    private Context context;

    DatesFragmentAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) return datesListFragment;
        else return datesCalenderFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) return context.getResources().getString(R.string.dates_list);
        else return context.getResources().getString(R.string.dates_calendar);
    }
}
