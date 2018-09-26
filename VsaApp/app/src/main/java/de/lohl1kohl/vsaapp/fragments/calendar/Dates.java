package de.lohl1kohl.vsaapp.fragments.calendar;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Dates extends BaseLoader {

    public void updateDates(Callbacks.baseCallback c) {
        TAG = "Dates";
        url = "https://api.vsa.lohl1kohl.de/dates/list.json";

        this.get(c);
    }
}
