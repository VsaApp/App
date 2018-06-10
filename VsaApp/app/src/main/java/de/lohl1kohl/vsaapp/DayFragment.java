package de.lohl1kohl.vsaapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DayFragment extends Fragment {

    JSONObject data;
    private Map<String, String> subjectsSymbols;

    public DayFragment() {

    }

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
            LinearLayout ll = root.findViewById(R.id.sp_day_ll);
            try {
                JSONArray l = data.getJSONArray("lessons");
                for (int j = 0; j < l.length(); j++) {
                    JSONArray g = l.getJSONArray(j);
                    List<Lesson> ls = new ArrayList<>();
                    if (g.length() > 0) {
                        if (g.length() > 1) {
                            for (int k = 0; k < g.length(); k++) {
                                Lesson lesson = new Lesson(data.getString("name"), j + 1, g.getJSONObject(k).getString("lesson"), g.getJSONObject(k).getString("room"), g.getJSONObject(k).getString("tutor"), subjectsSymbols);
                                ls.add(lesson);
                            }
                        } else {
                            Lesson lesson = new Lesson(data.getString("name"), j + 1, g.getJSONObject(0).getString("lesson"), g.getJSONObject(0).getString("room"), g.getJSONObject(0).getString("tutor"), subjectsSymbols);
                            ls.add(lesson);
                        }
                    }
                    for (Lesson lesson : ls) {
                        View cell = inflater.inflate(R.layout.sp_cell, container, false);
                        ((TextView) cell.findViewById(R.id.sp_lesson)).setText(lesson.getName());
                        ((TextView) cell.findViewById(R.id.sp_tutor)).setText(lesson.tutor);
                        ((TextView) cell.findViewById(R.id.sp_room)).setText(lesson.room);
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> ll.addView(cell));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

        return root;
    }
}