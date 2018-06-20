package de.lohl1kohl.vsaapp;

import java.util.Map;

public class Subject {
    public String day;
    public int unit;
    public String name;
    public String room;
    public String tutor;
    public Subject changes;
    private Map<String, String> subjectSymbols;

    Subject(String day, int unit, String name, String room, String tutor, Map<String, String> subjectSymbols) {
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
