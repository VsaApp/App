package de.lohl1kohl.vsaapp.fragments.vp;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Tomorrow extends BaseLoader {

    public void updateVp(String gradename, Callbacks.baseCallback c) {
        TAG = "Vp";
        url = "https://api.vsa.lohl1kohl.de/vp/tomorrow/%s.json";

        url = String.format(url, gradename);
        this.get(c);
    }
}
