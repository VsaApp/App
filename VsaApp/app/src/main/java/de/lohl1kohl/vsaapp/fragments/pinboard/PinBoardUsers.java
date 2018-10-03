package de.lohl1kohl.vsaapp.fragments.pinboard;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class PinBoardUsers extends BaseLoader {

    public void getUsers(Callbacks.baseCallback c) {
        TAG = "PinBoardUsers";
        url = "https://api.pinboard.vsa.lohl1kohl.de/api/users";

        this.get(c);
    }
}
