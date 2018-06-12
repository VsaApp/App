package de.lohl1kohl.vsaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;
import java.util.Map;

public class Lesson {
    private int currentIndex = 0;
    private List<Unit> units;

    Lesson(List<Unit> units) {
        this.units = units;
    }

    public void addUnit(Unit unit){
        units.add(unit);
    }

    public int numberOfUnits(){
        return units.size();
    }

    public void setUnit(int operation){
        currentIndex = (currentIndex + operation) % units.size();
        if (currentIndex < 0) currentIndex += units.size();
    }

    public Unit getUnit() {
        return units.get(currentIndex);
    }

    public void saveUnit(Context context){
        // Get unit...
        Unit unit = getUnit();

        // Save the current sp...
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        String prefName = String.format("pref_selectedUnit%s:%s:%s", settings.getString("pref_grade", "-1"), unit.day, Integer.toString(unit.unit));
        String prefValue = String.format("%s:%s", unit.name, unit.tutor);
        editor.putString(prefName, prefValue);
        editor.apply();
    }

    public void readSavedUnit(Context context){
        // Get the saved unit...
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = String.format("pref_selectedUnit%s:%s:%s", settings.getString("pref_grade", "-1"), getUnit().day, Integer.toString(getUnit().unit));
        String savedUnit = settings.getString(prefName,"-1");

        if (!savedUnit.equals("-1")){
            String[] values = savedUnit.split(":");
            for (Unit unit : units){
                if (unit.name.equals(values[0]) && unit.tutor.equals(values[1])) currentIndex = units.indexOf(unit);
            }
        }
    }
}
