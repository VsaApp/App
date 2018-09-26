package de.lohl1kohl.vsaapp.fragments.web.connections;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Delete extends BaseLoader {

    public void delete(String id, Callbacks.baseCallback c) {
        TAG = "Web";
        url = "https://vsa.lohl1kohl.de/delete?web=%s";

        url = String.format(url, id);
        this.get(c);
    }
}
