package de.lohl1kohl.vsaapp.fragments.documents;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.Callbacks;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class Documents implements AsyncResponse {

    private Callbacks.documentsCallback documentsCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            documentsCallback.onConnectionFailed();
        } else {
            documentsCallback.onReceived(output);
        }
    }

    public void getDocuments(Callbacks.documentsCallback c) {
        documentsCallback = c;

        String url = "https://api.vsa.lohl1kohl.de/documents/list.json";
        Log.i("VsaApp/Server/Documents", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
