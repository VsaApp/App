package de.lohl1kohl.vsaapp.loader;

public class Sums extends BaseLoader {

    static {
        TAG = "Sums";
        url = "https://api.vsa.lohl1kohl.de/sums/list.json";
    }

    public void getSums(Callbacks.baseCallback c) {
        this.get(c);
    }
}
