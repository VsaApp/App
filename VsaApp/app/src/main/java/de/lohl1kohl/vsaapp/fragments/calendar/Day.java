package de.lohl1kohl.vsaapp.fragments.calendar;

import java.util.ArrayList;
import java.util.List;

public class Day {
    public int day;
    public int month;
    public int year;
    private List<Event> events = new ArrayList<>();

    public Day(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public long getTimestemp() {
        return ((long) year * (long) 100000000) + ((month + 1) * 1000000) + (day * 10000);
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public List<Event> getEvents() {
        return events;
    }

    public Event getEvent(int index) {
        return events.get(index);
    }

}
