package de.lohl1kohl.vsaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

public class Lesson {
    private List<Subject> subjects;
    private int currentIndex = 0;

    public Lesson(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public boolean isGray() {
        try {
            boolean isPassed = LessonUtils.isLessonPassed(getSubject().unit);
            boolean isFuture = LessonUtils.isDayInFuture(getSubject().day);
            if (LessonUtils.isDayPassed(getSubject().day)) return true;
            else if (isPassed && !isFuture) {
                return true;
            }
        } catch (Exception ignored) {

        }

        return false;
    }

    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    public boolean containsSubject(String name){
        for (int i = 0; i < subjects.size(); i++){
            if (subjects.get(i).getName().equals(name)) return true;
        }
        return  false;
    }

    public int numberOfSubjects() {
        return subjects.size();
    }

    public Subject getSubject() {
        return subjects.get(currentIndex);
    }

    public void setSubject(int operation) {
        currentIndex = (currentIndex + operation) % subjects.size();
        if (currentIndex < 0) currentIndex += subjects.size();
    }

    public Subject getSubject(int i) {
        return subjects.get(i);
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Subject subject : subjects) {
            output.append(subject.getName()).append(":");
        }
        return output.toString();
    }

    public void saveSubject(Context context) {
        // Get subject...
        Subject subject = getSubject();

        // Save the current sp...
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        String prefName = String.format("pref_selectedSubject%s:%s:%s", settings.getString("pref_grade", "-1"), subject.day, Integer.toString(subject.unit));
        String prefValue = String.format("%s:%s", subject.name, subject.teacher);
        editor.putString(prefName, prefValue);
        editor.apply();
    }

    public void readSavedSubject(Context context) {

        // Get the saved Subject...
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = String.format("pref_selectedSubject%s:%s:%s", settings.getString("pref_grade", "-1"), getSubject().day, Integer.toString(getSubject().unit));
        String savedSubject = settings.getString(prefName, "-1");

        if (!savedSubject.equals("-1")) {
            String[] values = savedSubject.split(":");
            for (Subject subject : subjects) {
                if (subject.name.equals(values[0]) && subject.teacher.equals(values[1]))
                    currentIndex = subjects.indexOf(subject);
            }
        }
    }
}
