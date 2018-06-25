package de.lohl1kohl.vsaapp.holder;

public class Callbacks {
    public interface spLoadedCallback {
        void onOldLoaded();

        void onNewLoaded();

        void onConnectionFailed();

        void onNoSp();
    }

    public interface datesLoadedCallback {
        void onFinished();

        void onConnectionFailed();
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

    public interface cafetoriaLoadedCallback {
        void onOldLoaded();

        void onNewLoaded();

        void onConnectionFailed();
    }
}
