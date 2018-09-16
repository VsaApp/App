package de.lohl1kohl.vsaapp.fragments.vp;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Tomorrow extends BaseLoader {
    static {
        TAG = "Vp";
        url = "https://api.vsa.lohl1kohl.de/vp/tomorrow/%s.json";
    }

    public void updateVp(String gradename, Callbacks.baseCallback c) {
        url = String.format(url, gradename);
        this.get(c);
    }
}
