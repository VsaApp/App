package de.lohl1kohl.vsaapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import de.lohl1kohl.vsaapp.holder.VpHolder;

public class VpDayAdapter extends FragmentStatePagerAdapter {

    private VpDayFragment[] fragments = new VpDayFragment[]{new VpDayFragment(), new VpDayFragment()};

    VpDayAdapter(FragmentManager fm) {
        super(fm);
        fragments[0].today = true;
        fragments[1].today = false;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (fragments[position].today) return VpHolder.weekdayToday;
        else return VpHolder.weekdayTomorrow;
    }
}
