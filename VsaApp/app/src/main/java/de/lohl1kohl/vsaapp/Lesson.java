package de.lohl1kohl.vsaapp;

import java.util.Map;

public class Lesson {
    public String day;
    public int unit;
    public String name;
    public String room;
    public String tutor;
    public Lesson changes;
    boolean changed = false;
    private Map<String, String> subjectSymbols;

    Lesson(String day, int unit, String name, String room, String tutor, Map<String, String> subjectSymbols) {
        this.day = day;
        this.unit = unit;
        this.name = name;
        this.room = room;
        this.tutor = tutor;
        this.subjectSymbols = subjectSymbols;
    }

    public String getName() {
        if (subjectSymbols.containsKey(name.toUpperCase())) {
            return subjectSymbols.get(name.toUpperCase());
        }
        return name;
    }
}
