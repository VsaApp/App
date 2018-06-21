package de.lohl1kohl.vsaapp.holder;

public class Callbacks {
    public interface spLoadedCallback {
        void onFinished();

        void onConnectionFailed();

        void onNoSp();
    }

    public interface vpLoadedCallback {
        void onFinished();

        void onConnectionFailed();
    }
}
