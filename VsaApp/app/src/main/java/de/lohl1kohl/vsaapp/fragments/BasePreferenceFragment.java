package de.lohl1kohl.vsaapp.fragments;

import android.app.Activity;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;

public abstract class BasePreferenceFragment extends PreferenceFragment {

    protected FragmentActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }
}