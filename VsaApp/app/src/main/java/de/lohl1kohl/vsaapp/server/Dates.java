package de.lohl1kohl.vsaapp.server;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class Dates implements AsyncResponse {
    private Callbacks.datesCallback datesCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            datesCallback.onConnectionFailed();
        } else {
            datesCallback.onReceived(output);
        }
    }

    public void updateDates(Callbacks.datesCallback c) {
        datesCallback = c;

        String url = "https://vsa.lohl1kohl.de/dates/list.json";
        Log.i("VsaApp/Server/Dates", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }

}
