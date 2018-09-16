package de.lohl1kohl.vsaapp.fragments.teachers;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Teachers extends BaseLoader {

    static {
        TAG = "Teachers";
        url = "https://api.vsa.lohl1kohl.de/teachers/list.json";
    }

    public void updateSp(Callbacks.baseCallback c) {
        this.get(c);
    }
}
