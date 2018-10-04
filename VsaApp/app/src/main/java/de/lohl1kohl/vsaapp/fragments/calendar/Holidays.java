package de.lohl1kohl.vsaapp.fragments.calendar;

import java.util.Locale;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Holidays extends BaseLoader {

    public void updateDates(Callbacks.baseCallback c, int year) {
        TAG = "Holidays";
        url = String.format(Locale.GERMAN, "https://feiertage-api.de/api/?jahr=%d&nur_land=NW", year);

        this.get(c);
    }
}
