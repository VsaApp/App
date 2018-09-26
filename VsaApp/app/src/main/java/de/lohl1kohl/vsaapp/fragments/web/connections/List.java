package de.lohl1kohl.vsaapp.fragments.web.connections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class List extends BaseLoader {

    @SuppressLint("HardwareIds")
    private String getMyID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void list(Context context, Callbacks.baseCallback c) {
        TAG = "Web";
        url = String.format("https://vsa.lohl1kohl.de/connections?id=%s", getMyID(context));
        this.get(c);
    }
}
