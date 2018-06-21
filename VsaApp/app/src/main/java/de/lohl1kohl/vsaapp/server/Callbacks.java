package de.lohl1kohl.vsaapp.server;

public class Callbacks {

    public interface vpCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface spCallback {
        void onReceived(String output);

        void onConnectionFailed();

        void onNoSp();
    }

    public interface teachersCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface credentialsCallback {
        void onSuccess();

        void onFailed();

        void onConnectionFailed();
    }
}
