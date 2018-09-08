package de.lohl1kohl.vsaapp.fragments.web.connections;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.Callbacks;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class Delete implements AsyncResponse {

    private Callbacks.deleteCallback deleteCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            deleteCallback.onConnectionFailed();
        } else {
            deleteCallback.onReceived(output);
        }
    }

    public void delete(String id, Callbacks.deleteCallback c) {
        deleteCallback = c;

        String url = String.format("https://vsa.lohl1kohl.de/delete?web=%s", id);
        Log.i("VsaApp/Server/Web", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
