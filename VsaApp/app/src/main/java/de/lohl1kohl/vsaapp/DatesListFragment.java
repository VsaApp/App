package de.lohl1kohl.vsaapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.lohl1kohl.vsaapp.holder.DatesHolder;

public class DatesListFragment extends BaseFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dates_list, container, false);

        ListView listView = root.findViewById(R.id.datesList);
        DatesListAdapter datesListAdapter = new DatesListAdapter(mActivity, DatesHolder.getFiltertCalendar(mActivity));
        listView.setAdapter(datesListAdapter);

        return root;
    }

}
