package de.lohl1kohl.vsaapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import de.lohl1kohl.vsaapp.holder.SpHolder;

public class SpDayFragment extends BaseFragment {

    int day;

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sp_day, container, false);
        new Thread(() -> {
            List<Lesson> spDay = SpHolder.getDay(day);
            String weekday = Arrays.asList(root.getResources().getStringArray(R.array.weekdays)).get(day);
            ListView lV = root.findViewById(R.id.sp_day);

            // Ignore the last free lessons...
            for (int i = spDay.size() - 1; i >= 0; i--) {
                if (spDay.get(i).numberOfSubjects() > 0) break;
                else spDay.remove(i);
            }

            // If the grade is EF, Q1 or Q2 add the option for a free lesson...
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String grade = sharedPref.getString("pref_grade", "-1").toUpperCase();

            if (grade.equals("EF") | grade.equals("Q1") | grade.equals("Q2")) {
                for (int unit = 0; unit < spDay.size(); unit++) {
                    Lesson lesson = spDay.get(unit);
                    if (unit == 5) continue;
                    lesson.addSubject(new Subject(weekday, unit, getResources().getString(R.string.lesson_free), "", ""));
                }
            }

            mActivity.runOnUiThread(() -> lV.setAdapter(new SpDayListAdapter(mActivity, spDay)));
        }).start();

        return root;
    }
}