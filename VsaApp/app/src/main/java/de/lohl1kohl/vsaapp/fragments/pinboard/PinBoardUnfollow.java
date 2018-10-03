package de.lohl1kohl.vsaapp.fragments.pinboard;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class PinBoardUnfollow extends BaseLoader {

    public void unfollow(String user, Callbacks.baseCallback c) {
        TAG = "PinBoardUnfollow";
        url = "https://api.pinboard.vsa.lohl1kohl.de/api/unfollow?username=%s";

        url = String.format(url, user);
        this.get(c);
    }
}
