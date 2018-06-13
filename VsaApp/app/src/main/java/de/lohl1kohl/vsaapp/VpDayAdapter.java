package de.lohl1kohl.vsaapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.json.JSONException;

import java.util.List;

public class VpDayAdapter extends FragmentStatePagerAdapter {

    private VpDayFragment[] fragments = new VpDayFragment[]{new VpDayFragment(), new VpDayFragment()};

    VpDayAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setDataToday(List<Subject> data) {
        fragments[0].setData(data);
    }

    public void setDataTomorrow(List<Subject> data) {
        fragments[1].setData(data);
    }

    public void setInfoToday(String weekday, String date, String time) {
        fragments[0].setInfo(weekday, date, time);
    }

    public void setInfoTomorrow(String weekday, String date, String time) {
        fragments[1].setInfo(weekday, date, time);
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
        return fragments[position].weekday;
    }
}
