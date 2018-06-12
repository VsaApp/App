package de.lohl1kohl.vsaapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

public class DayFragment extends Fragment {

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
            ListView lV = root.findViewById(R.id.sp_day);
            List<Lesson> spDay = new ArrayList<Lesson>();
            try {
                JSONArray l = data.getJSONArray("lessons");
                for (int j = 0; j < l.length(); j++) {
                    JSONArray g = l.getJSONArray(j);
                    Lesson ls = new Lesson(new ArrayList<Unit>());
                    if (g.length() > 0) {
                        if (g.length() > 1) {
                            for (int k = 0; k < g.length(); k++) {
                                Unit unit = new Unit(data.getString("name"), j + 1, g.getJSONObject(k).getString("lesson"), g.getJSONObject(k).getString("room"), g.getJSONObject(k).getString("tutor"), subjectsSymbols);
                                ls.addUnit(unit);
                            }
                        } else {
                            Unit unit = new Unit(data.getString("name"), j + 1, g.getJSONObject(0).getString("lesson"), g.getJSONObject(0).getString("room"), g.getJSONObject(0).getString("tutor"), subjectsSymbols);
                            ls.addUnit(unit);
                        }
                    }

                    if (ls.numberOfUnits() > 0) {
                        ls.readSavedUnit(getContext());
                        spDay.add(ls);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> lV.setAdapter(new SpDayAdapter(getContext(), spDay)));
        }).start();

        return root;
    }
}