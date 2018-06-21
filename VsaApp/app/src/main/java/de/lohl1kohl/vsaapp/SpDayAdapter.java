package de.lohl1kohl.vsaapp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SpDayAdapter extends FragmentStatePagerAdapter {

    private List<SpDayFragment> fragments = new ArrayList<>();
    private Context context;

    SpDayAdapter(Context context, FragmentManager fm) {
        super(fm);
        for (int i = 0; i < 5; i++) {
            fragments.add(new SpDayFragment());
            fragments.get(fragments.size() - 1).setDay(i);
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Arrays.asList(context.getResources().getStringArray(R.array.weekdays)).get(position).substring(0, 2);
    }
}
