package de.lohl1kohl.vsaapp.fragments.cafetoria;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.Callbacks;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class Cafetoria implements AsyncResponse {

    private Callbacks.cafetoriaCallback cafetoriaCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            cafetoriaCallback.onConnectionFailed();
        } else {
            cafetoriaCallback.onReceived(output);
        }
    }

    public void updateMenues(String id, String password, Callbacks.cafetoriaCallback c) {
        cafetoriaCallback = c;

        String url = String.format("https://api.vsa.lohl1kohl.de/cafetoria?id=%s&password=%s", id, password);
        Log.i("VsaApp/Server/Cafetoria", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
