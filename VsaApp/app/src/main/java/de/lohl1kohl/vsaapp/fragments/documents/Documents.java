package de.lohl1kohl.vsaapp.fragments.documents;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Documents extends BaseLoader {

    public void getDocuments(Callbacks.baseCallback c) {
        TAG = "Documents";
        url = "https://api.vsa.lohl1kohl.de/documents/list.json";

        this.get(c);
    }
}
