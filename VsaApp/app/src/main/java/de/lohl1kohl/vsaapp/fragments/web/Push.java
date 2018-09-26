package de.lohl1kohl.vsaapp.fragments.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import org.json.JSONArray;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Push extends BaseLoader {

    @SuppressLint("HardwareIds")
    private String getMyID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void push(Context context, JSONArray choice, Callbacks.baseCallback c) {
        TAG = "Web";
        url = "https://vsa.lohl1kohl.de/push?id=%s&choice=%s";

        url = String.format(url, getMyID(context), choice.toString());
        this.get(c);
    }
}
