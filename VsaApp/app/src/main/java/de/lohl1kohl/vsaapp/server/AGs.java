package de.lohl1kohl.vsaapp.server;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class AGs implements AsyncResponse {

    private Callbacks.agsCallback agsCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            agsCallback.onConnectionFailed();
        } else {
            agsCallback.onReceived(output);
        }
    }

    public void getAGs(Callbacks.agsCallback c) {
        agsCallback = c;

        String url = "https://vsa.lohl1kohl.de/ags/list.json";
        Log.i("VsaApp/Server/AGs", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
