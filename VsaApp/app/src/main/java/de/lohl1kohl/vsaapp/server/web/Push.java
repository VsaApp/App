package de.lohl1kohl.vsaapp.server.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;
import de.lohl1kohl.vsaapp.server.Callbacks;

public class Push implements AsyncResponse {

    private Callbacks.pushCallback pushCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            pushCallback.onConnectionFailed();
        } else {
            pushCallback.onReceived(output);
        }
    }

    @SuppressLint("HardwareIds")
    private String getMyID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void push(Context context, JSONArray choice, Callbacks.pushCallback c) {
        pushCallback = c;

        String url = String.format("https://vsa.lohl1kohl.de/push?id=%s&choice=%s", getMyID(context), choice.toString());
        Log.i("VsaApp/Server/Web", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
