package de.lohl1kohl.vsaapp.holder;

public class Callbacks {
    public interface spLoadedCallback {
        void onOldLoaded();

        void onNewLoaded();

        void onConnectionFailed();

        void onNoSp();
    }

    public interface vpLoadedCallback {
        void onFinished();

        void onConnectionFailed();
    }

    public interface teachersLoadedCallback {
        void onOldLoaded();

        void onNewLoaded();

        void onConnectionFailed();
    }
}
