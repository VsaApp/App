package de.lohl1kohl.vsaapp.server;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class Sp implements AsyncResponse {

    private Callbacks.spCallback spCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            spCallback.onConnectionFailed();
        } else if (output.equals("404")) {
            spCallback.onNoSp();
        } else {
            spCallback.onReceived(output);
        }
    }

    public void updateSp(String gradename, Callbacks.spCallback c) {
        spCallback = c;

        String url = String.format("https://vsa.lohl1kohl.de/sp/%s.json", gradename);
        Log.i("VsaApp/Server/Sp", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
