package de.lohl1kohl.vsaapp;

public class Event {
    public String name;
    public String info;
    public Date start;
    public Date end;

    public Event(String name, String info, Date start, Date end) {
        this.name = name;
        this.info = info;
        this.start = start;
        this.end = end;
    }

    public Event(String name, Date start, Date end) {
        this.name = name;
        this.info = "";
        this.start = start;
        this.end = end;
    }
}
