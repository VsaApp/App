package de.lohl1kohl.vsaapp.server.vp;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;
import de.lohl1kohl.vsaapp.server.Callbacks;

public class Today implements AsyncResponse {
    private Callbacks.vpCallback vpCallback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            vpCallback.onConnectionFailed();
        } else {
            vpCallback.onReceived(output);
        }
    }

    public void updateVp(String gradename, Callbacks.vpCallback c) {
        vpCallback = c;
        String url = String.format("https://vsa.lohl1kohl.de/vp/today/%s.json", gradename);
        Log.i("VsaApp/Server/Vp", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
