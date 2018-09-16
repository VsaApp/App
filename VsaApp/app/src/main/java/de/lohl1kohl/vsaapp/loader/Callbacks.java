package de.lohl1kohl.vsaapp.loader;

public class Callbacks {

    public interface baseLoadedCallback {
        void onOldLoaded();

        void onNewLoaded();

        void onConnectionFailed();
    }

    public interface baseCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface credentialsCallback {
        void onSuccess();

        void onFailed();

        void onConnectionFailed();
    }
}
