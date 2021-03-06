package de.lohl1kohl.vsaapp.fragments.vp;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import de.lohl1kohl.vsaapp.holders.VpHolder;

import java.util.ArrayList;
import java.util.List;

public class VpDayAdapter extends FragmentStatePagerAdapter {

    public List<VpDayFragment> fragments = new ArrayList<>();

    VpDayAdapter(FragmentManager fm) {
        super(fm);

        VpDayFragment today = new VpDayFragment();
        today.today = true;
        fragments.add(today);

        VpDayFragment tomorrow = new VpDayFragment();
        tomorrow.today = false;
        fragments.add(tomorrow);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        int index = fragments.indexOf(object);
        if (index == -1) {
            return POSITION_NONE;
        } else {
            return index;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (fragments.get(position).today) return VpHolder.weekdayToday;
        else return VpHolder.weekdayTomorrow;
    }
}
