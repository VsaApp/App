package de.lohl1kohl.vsaapp.fragments.calendar;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import de.lohl1kohl.vsaapp.R;

public class DatesFragmentAdapter extends FragmentStatePagerAdapter {

    private DatesListFragment datesListFragment = new DatesListFragment();
    private DatesCalendarFragment datesCalendarFragment = new DatesCalendarFragment();
    private Context context;

    DatesFragmentAdapter(DatesFragment parent, Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        datesListFragment.parent = parent;
        datesCalendarFragment.parent = parent;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) return datesListFragment;
        else return datesCalendarFragment;
    }

    public void update(DatesFragment parent) {
        datesListFragment.update();
        datesCalendarFragment.update();
        notifyDataSetChanged();
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
