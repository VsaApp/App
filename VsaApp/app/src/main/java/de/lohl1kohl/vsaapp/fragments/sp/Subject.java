package de.lohl1kohl.vsaapp.fragments.sp;

import de.lohl1kohl.vsaapp.holders.SubjectSymbolsHolder;

public class Subject {
    public String day;
    public int unit;
    public String name;
    public String room;
    public String teacher;
    public Subject changes;

    public Subject(String day, int unit, String name, String room, String teacher) {
        this.day = day;
        this.unit = unit;
        this.name = name;
        this.room = room;
        this.teacher = teacher;
    }

    public String getName() {
        if (SubjectSymbolsHolder.has(name.toUpperCase())) {
            return SubjectSymbolsHolder.get(name.toUpperCase());
        }
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        Subject s = (Subject) obj;
        return day.equals(s.day) && unit == s.unit && name.equals(s.name) && room.equals(s.room) && teacher.equals(s.teacher);
    }
}
