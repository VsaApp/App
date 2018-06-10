package de.lohl1kohl.vsaapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DayAdapter extends FragmentPagerAdapter {

    private List<DayFragment> fragments = new ArrayList<>();

    DayAdapter(FragmentManager fm, JSONArray data, Map<String, String> subjectsSymbols) {
        super(fm);
        for (int i = 0; i < data.length(); i++) {
            try {
                fragments.add(new DayFragment());
                fragments.get(fragments.size() - 1).setData(data.getJSONObject(i));
                fragments.get(fragments.size() - 1).setSubjectSymbols(subjectsSymbols);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        try {
            return fragments.get(position).data.getString("name").substring(0, 2);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
