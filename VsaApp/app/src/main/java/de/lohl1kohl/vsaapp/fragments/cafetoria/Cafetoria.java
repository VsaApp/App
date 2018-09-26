package de.lohl1kohl.vsaapp.fragments.cafetoria;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Cafetoria extends BaseLoader {

    public void updateMenues(String id, String password, Callbacks.baseCallback c) {
        TAG = "Cafetoria";
        url = "https://api.vsa.lohl1kohl.de/cafetoria?id=%s&password=%s";

        url = String.format(url, id, password);
        this.get(c);
    }
}
