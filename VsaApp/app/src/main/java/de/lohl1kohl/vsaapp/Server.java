package de.lohl1kohl.vsaapp;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Server implements AsyncResponse {
    private credentialsCallback credentialsCallback;
    private spCallback spCallback;
    private vpCallback vpCallback;
    private String waitForMsg;
    private boolean connectedToServer = false;

    Server() {
    }

    // utility function
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    @Override
    public void processFinish(String output) {
        if (output != null) {
            connectedToServer = true;
        }
        if (waitForMsg.equals("loginData")) {
            credentialFinish(output);
        } else if (waitForMsg.equals("spData")) {
            spFinish(output);
        } else if (waitForMsg.equals("vpData")) {
            vpFinish(output);
        }
    }

    private void vpFinish(String output){
        if (output == null) {
            vpCallback.onConnectionFailed();
        } else {
            vpCallback.onReceived(output);
        }
    }

    private void spFinish(String output) {
        if (output == null) {
            spCallback.onConnectionFailed();
        } else {
            spCallback.onReceived(output);
        }
    }

    private void credentialFinish(String output) {
        if (output == null) {
            credentialsCallback.onConnectionFailed();
        } else if (output.equals("0")) {
            credentialsCallback.onSuccess();
        } else {
            credentialsCallback.onFailed();
        }
    }

    public void login(String username, String password, credentialsCallback c) {
        credentialsCallback = c;
        waitForMsg = "loginData";

        MessageDigest digest;
        String url, hashPassword = "", hashUsername = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(username.getBytes());
            hashUsername = bytesToHexString(digest.digest());
            digest.update(password.getBytes());
            hashPassword = bytesToHexString((digest.digest()));
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        url = String.format("https://vsa.lohl1kohl.de/validate?username=%s&password=%s", hashUsername, hashPassword);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }

    public void updateSp(String classname, spCallback c) {
        spCallback = c;
        waitForMsg = "spData";

        String url = String.format("https://vsa.lohl1kohl.de/sp/%s.json", classname);
        Log.i("VsaApp/Server", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }

    public void updateVp(String classname, vpCallback c, String day) {
        vpCallback = c;
        waitForMsg = "vpData";

        String url = String.format("https://vsa.lohl1kohl.de/vp/%s/%s.json", day, classname);
        Log.i("VsaApp/Server", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }

    public interface vpCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface spCallback {
        void onReceived(String output);

        void onConnectionFailed();
    }

    public interface credentialsCallback {
        void onSuccess();

        void onFailed();

        void onConnectionFailed();
    }
}
