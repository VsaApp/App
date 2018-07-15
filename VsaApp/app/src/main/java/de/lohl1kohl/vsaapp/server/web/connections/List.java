package de.lohl1kohl.vsaapp.server.web.connections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;
import de.lohl1kohl.vsaapp.server.Callbacks;

public class List implements AsyncResponse {

    private Callbacks.connectionsCallback connectionsCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            connectionsCallback.onConnectionFailed();
        } else {
            connectionsCallback.onReceived(output);
        }
    }

    @SuppressLint("HardwareIds")
    private String getMyID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void list(Context context, Callbacks.connectionsCallback c) {
        connectionsCallback = c;

        String url = String.format("https://vsa.lohl1kohl.de/web/connections?id=%s", getMyID(context));
        Log.i("VsaApp/Server/Web", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
