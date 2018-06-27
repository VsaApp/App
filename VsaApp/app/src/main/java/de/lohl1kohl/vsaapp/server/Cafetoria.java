package de.lohl1kohl.vsaapp.server;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class Cafetoria implements AsyncResponse {

    private Callbacks.cafetoriaCallback spCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            spCallback.onConnectionFailed();
        } else {
            spCallback.onReceived(output);
        }
    }

    public void updateMenues(String id, String password, Callbacks.cafetoriaCallback c) {
        spCallback = c;

        String url = String.format("https://vsa.lohl1kohl.de/cafetoria?id=%s&password=%s", id, password);
        Log.i("VsaApp/Server/Sp", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}