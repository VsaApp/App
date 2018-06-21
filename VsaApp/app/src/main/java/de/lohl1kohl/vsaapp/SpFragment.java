package de.lohl1kohl.vsaapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.Callbacks.spLoadedCallback;
import de.lohl1kohl.vsaapp.holder.SpHolder;


public class SpFragment extends BaseFragment {
    private View spView;
    private Map<String, String> subjectsSymbols = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        spView = inflater.inflate(R.layout.fragment_sp, container, false);


        // Create dictionary with all subject symbols...
        String[] subjects = mActivity.getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");

            subjectsSymbols.put(pair[0], pair[1]);
        }


        // Try to refresh the sp...
        syncSp();

        return spView;
    }

    public void syncSp() {
        // Get gradename...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String grade = sharedPref.getString("pref_grade", "-1");

        // Check if a gradename is set...
        if (grade.equals("-1")) {
            return;
        }

        // Create callback...
        spLoadedCallback callback = new spLoadedCallback() {
            @Override
            public void onOldLoaded() {
                fillSp();
            }

            @Override
            public void onNewLoaded() {
                fillSp();
            }

            @Override
            public void onConnectionFailed() {
                Log.e("VsaApp/Server", "Failed");
                Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();
                fillSp();
            }

            @Override
            public void onNoSp() {
                TabLayout tabLayout = spView.findViewById(R.id.sp_tabs);
                tabLayout.setVisibility(View.GONE);
                TextView text = spView.findViewById(R.id.noSp);
                text.setVisibility(View.VISIBLE);
                text.setText(R.string.noSp);
            }
        };
        new Thread(() -> SpHolder.load(mActivity, true, callback)).start();
    }

    public void fillSp() {
        mActivity.runOnUiThread(() -> {
            ViewPager pager = spView.findViewById(R.id.sp_viewpager);
            SpDayAdapter adapter = new SpDayAdapter(mActivity, getFragmentManager());
            pager.setAdapter(adapter);
            TabLayout tabLayout = spView.findViewById(R.id.sp_tabs);
            tabLayout.setupWithViewPager(pager);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            try {
                if (weekday == -1 | weekday == 5) weekday = 0;
                else if (LessonUtils.isLessonPassed(SpHolder.getDay(weekday).size() - 1)) weekday++;
                TabLayout.Tab tab = tabLayout.getTabAt(weekday);
                Objects.requireNonNull(tab).select();
            } catch (IndexOutOfBoundsException ignored) {

            }
        });
    }
}