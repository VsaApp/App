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

    public interface datesCallback {
        void onReceived(String output);

        void onConnectionFailed();
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

    public interface cafetoriaCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface connectCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface connectionsCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface pushCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface deleteCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface agsCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }
}
