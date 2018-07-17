package de.lohl1kohl.vsaapp.server;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class Teachers implements AsyncResponse {

    private Callbacks.teachersCallback teachersCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            teachersCallback.onConnectionFailed();
        } else {
            teachersCallback.onReceived(output);
        }
    }

    public void updateSp(Callbacks.teachersCallback c) {
        teachersCallback = c;

        String url = "https://vsa.lohl1kohl.de/teachers/list.json";
        Log.i("VsaApp/Server/Teachers", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
