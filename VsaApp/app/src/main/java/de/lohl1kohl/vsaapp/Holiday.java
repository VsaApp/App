package de.lohl1kohl.vsaapp;

import de.lohl1kohl.vsaapp.server.Dates;

public class Holiday {
    public Date start;
    public Date end;
    public String name;

    public Holiday(String name, Date start, Date end){
        this.name = name;
        this.start = start;
        this.end = end;
    }
}
