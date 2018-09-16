package de.lohl1kohl.vsaapp.fragments.documents;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Documents extends BaseLoader {

    static {
        TAG = "Documents";
        url = "https://api.vsa.lohl1kohl.de/documents/list.json";
    }

    public void getDocuments(Callbacks.baseCallback c) {
        this.get(c);
    }
}
