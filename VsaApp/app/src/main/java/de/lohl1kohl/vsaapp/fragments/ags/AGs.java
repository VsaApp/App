package de.lohl1kohl.vsaapp.fragments.ags;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class AGs extends BaseLoader {

    static {
        TAG = "AGs";
        url = "https://api.vsa.lohl1kohl.de/ags/list.json";
    }

    public void getAGs(Callbacks.baseCallback c) {
        this.get(c);
    }
}
