package de.lohl1kohl.vsaapp.fragments.sp;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Sp extends BaseLoader {

    static {
        TAG = "Sp";
        url = "https://api.vsa.lohl1kohl.de/sp/%s.json";
    }

    public void updateSp(String gradename, Callbacks.baseCallback c) {
        url = String.format(url, gradename);
        this.get(c);
    }
}
