package de.lohl1kohl.vsaapp;

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
