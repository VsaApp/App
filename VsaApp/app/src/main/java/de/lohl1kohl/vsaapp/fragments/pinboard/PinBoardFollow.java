package de.lohl1kohl.vsaapp.fragments.pinboard;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class PinBoardFollow extends BaseLoader {

    public void follow(String user, Callbacks.baseCallback c) {
        TAG = "PinBoardFollow";
        url = "https://api.pinboard.vsa.lohl1kohl.de/api/follow?username=%s";

        url = String.format(url, user);
        this.get(c);
    }
}
