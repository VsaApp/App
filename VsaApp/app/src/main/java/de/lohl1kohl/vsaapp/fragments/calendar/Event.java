package de.lohl1kohl.vsaapp.fragments.calendar;

import android.content.Context;

import java.util.Calendar;

public class Event {
    public String name;
    public String info;
    public Category category;
    public Date start;
    public Date end;

    public Event(String name, String info, Date start, Date end, Category category) {
        this.name = name;
        this.info = info;
        this.start = start;
        this.end = end;
        this.category = category;
    }

    public Event(String name, Date start, Date end, Category category) {
        this.name = name;
        this.info = "";
        this.start = start;
        this.end = end;
        this.category = category;
    }

    public long getStartTime(Context c){
        return start.getTimeInMillis(c);
    }

    public long getEndTime(Context c){
        return end.getTimeInMillis(c);
    }
}
