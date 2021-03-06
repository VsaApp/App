package de.lohl1kohl.vsaapp.loader;

import android.util.Log;
import de.lohl1kohl.vsaapp.HttpGetRequest;

public class BaseLoader implements AsyncResponse {

    public String TAG;
    public String url;
    public int readTimeout = 3000;
    private Callbacks.baseCallback callback;

    @Override
    public void processFinish(String output) {
        if (output == null) {
            callback.onConnectionFailed();
        } else {
            callback.onReceived(output);
        }
    }

    public void get(Callbacks.baseCallback c) {
        callback = c;
        Log.i("VsaApp/Server/" + TAG, "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest(readTimeout);

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
