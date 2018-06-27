package de.lohl1kohl.vsaapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class DatesFragment extends BaseFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dates, container, false);
        createPages(root);
        return root;
    }

    private void createPages(View view) {
        ViewPager pager = view.findViewById(R.id.dates_viewpager);
        DatesFragmentAdapter adapter = new DatesFragmentAdapter(mActivity, getFragmentManager());
        pager.setAdapter(adapter);

        // Add the tabs...
        TabLayout tabLayout = view.findViewById(R.id.dates_tabs);
        tabLayout.setupWithViewPager(pager);
    }
}
