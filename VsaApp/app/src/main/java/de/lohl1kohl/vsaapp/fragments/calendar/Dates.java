package de.lohl1kohl.vsaapp.fragments.calendar;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Dates extends BaseLoader {

    static {
        TAG = "Dates";
        url = "https://api.vsa.lohl1kohl.de/dates/list.json";
    }

    public void updateDates(Callbacks.baseCallback c) {
        this.get(c);
    }
}
