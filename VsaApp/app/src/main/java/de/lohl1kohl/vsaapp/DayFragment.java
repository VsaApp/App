package de.lohl1kohl.vsaapp;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DayFragment extends BaseFragment {

    JSONObject data;
    private Map<String, String> subjectsSymbols;

    public void setData(JSONObject data) {
        this.data = data;
    }

    public void setSubjectSymbols(Map<String, String> subjectsSymbols) {
        this.subjectsSymbols = subjectsSymbols;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sp_day, container, false);
        new Thread(() -> {
            String day = "";
            ListView lV = root.findViewById(R.id.sp_day);
            List<Lesson> spDay = new ArrayList<>();
            try {
                JSONArray l = data.getJSONArray("lessons");
                for (int j = 0; j < l.length(); j++) {
                    JSONArray g = l.getJSONArray(j);
                    Lesson ls = new Lesson(new ArrayList<>());
                    if (g.length() > 0) {
                        day = data.getString("name");
                        if (g.length() > 1) {
                            for (int k = 0; k < g.length(); k++) {
                                Subject subject = new Subject(data.getString("name"), j + 1, g.getJSONObject(k).getString("lesson"), g.getJSONObject(k).getString("room"), g.getJSONObject(k).getString("tutor"), subjectsSymbols);
                                ls.addSubject(subject);
                            }
                        } else {
                            Subject subject = new Subject(data.getString("name"), j + 1, g.getJSONObject(0).getString("lesson"), g.getJSONObject(0).getString("room"), g.getJSONObject(0).getString("tutor"), subjectsSymbols);
                            ls.addSubject(subject);
                        }
                    }

                    if (ls.numberOfSubjects() > 1) {
                        ls.readSavedSubject(mActivity);
                    }
                    spDay.add(ls);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Ignore the last free lessons...
            for (int i = spDay.size()-1; i >= 0; i--){
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
                    lesson.addSubject(new Subject(day, unit, getResources().getString(R.string.lesson_free), "", "", subjectsSymbols));
                }
            }

            Objects.requireNonNull(mActivity).runOnUiThread(() -> {
                lV.setAdapter(new SpDayAdapter(Objects.requireNonNull(mActivity.getApplicationContext()), spDay));
            });
        }).start();

        return root;
    }
}