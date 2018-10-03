package de.lohl1kohl.vsaapp.fragments.calendar;

public class Category {
    public Color color;
    public String name;
    public boolean isSchool;

    public Category(String name, Color color, boolean isSchool){
        this.name = name;
        this.color = color;
        this.isSchool = isSchool;
    }
}
