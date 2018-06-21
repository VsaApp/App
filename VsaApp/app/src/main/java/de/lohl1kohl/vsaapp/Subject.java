package de.lohl1kohl.vsaapp;

import de.lohl1kohl.vsaapp.holder.SpHolder;

public class Subject {
    public String day;
    public int unit;
    public String name;
    public String room;
    public String tutor;
    public Subject changes;

    public Subject(String day, int unit, String name, String room, String tutor) {
        this.day = day;
        this.unit = unit;
        this.name = name;
        this.room = room;
        this.tutor = tutor;
    }

    public String getName() {
        if (SpHolder.subjectsSymbols.containsKey(name.toUpperCase())) {
            return SpHolder.subjectsSymbols.get(name.toUpperCase());
        }
        return name;
    }
}
