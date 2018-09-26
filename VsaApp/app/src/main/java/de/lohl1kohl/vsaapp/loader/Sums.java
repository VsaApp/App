package de.lohl1kohl.vsaapp.loader;

public class Sums extends BaseLoader {

    public void getSums(Callbacks.baseCallback c) {
        TAG = "Sums";
        url = "https://api.vsa.lohl1kohl.de/sums/list.json";

        this.get(c);
    }
}
