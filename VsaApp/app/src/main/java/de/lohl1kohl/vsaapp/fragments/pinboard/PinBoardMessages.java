package de.lohl1kohl.vsaapp.fragments.pinboard;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class PinBoardMessages extends BaseLoader {

    public void getMessages(String user, Callbacks.baseCallback c) {
        TAG = "PinBoardMessages";
        url = "https://api.pinboard.vsa.lohl1kohl.de/api/messages?username=%s";

        url = String.format(url, user);
        this.get(c);
    }
}
