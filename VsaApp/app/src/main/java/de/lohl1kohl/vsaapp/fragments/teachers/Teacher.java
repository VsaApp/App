package de.lohl1kohl.vsaapp.fragments.teachers;

import android.content.Context;

import de.lohl1kohl.vsaapp.R;

public class Teacher {
    private String sName;
    private String lName;
    private String gender;
    private String[] subjects;
    private Context context;

    public Teacher(Context context, String shortName, String longName, String gender, String[] subjects) {
        this.context = context;
        sName = shortName;
        lName = longName;
        this.gender = gender;
        this.subjects = subjects;
    }

    public String getShortName() {
        return sName;
    }

    public String getLongName() {
        return lName;
    }

    public String getGenderizedName() {
        String name = lName;
        if (gender.equals("male")) {
            name = context.getResources().getString(R.string.mister) + " " + name;
        }
        if (gender.equals("female")) {
            name = context.getResources().getString(R.string.misses) + " " + name;
        }
        return name;
    }

    public String getGenderizedGenitiveName() {
        return getGenderizedName().replace(context.getResources().getString(R.string.mister) + " ", context.getResources().getString(R.string.mister_gen) + " ");
    }

    public String[] getSubjects() {
        return subjects;
    }
}