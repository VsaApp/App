package de.lohl1kohl.vsaapp.fragments.ags;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;

import java.util.Calendar;
import java.util.Date;


public class AGsFragment extends BaseFragment {
    @SuppressLint("StaticFieldLeak")
    private LayoutInflater inflater;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View root = inflater.inflate(R.layout.fragment_ags, container, false);
        createView(root);
        return root;
    }

    public void createView(View root) {
        mActivity.runOnUiThread(() -> {

            ViewPager pager = root.findViewById(R.id.ags_viewpager);
            AGsDayAdapter adapter = new AGsDayAdapter(mActivity, getFragmentManager());
            pager.setAdapter(adapter);
            TabLayout tabLayout = root.findViewById(R.id.ags_tabs);
            tabLayout.setupWithViewPager(pager);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            try {
                if (weekday == -1 | weekday == 5) weekday = 0;
                TabLayout.Tab tab = tabLayout.getTabAt(weekday);
                try {
                    tab.select();
                } catch (Exception ignored) {

                }
            } catch (IndexOutOfBoundsException ignored) {

            }
        });

    }
}