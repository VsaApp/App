package de.lohl1kohl.vsaapp.fragments.vp;

import android.util.Log;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.Callbacks;
import de.lohl1kohl.vsaapp.HttpGetRequest;

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
        String url = String.format("https://api.vsa.lohl1kohl.de/vp/today/%s.json", gradename);
        Log.i("VsaApp/Server/Vp", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
