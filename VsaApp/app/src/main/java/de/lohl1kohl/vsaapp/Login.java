package de.lohl1kohl.vsaapp;

import de.lohl1kohl.vsaapp.loader.AsyncResponse;
import de.lohl1kohl.vsaapp.loader.Callbacks;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login implements AsyncResponse {

    private Callbacks.credentialsCallback credentialsCallback;

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
        if (output == null) {
            credentialsCallback.onConnectionFailed();
        } else if (output.equals("0")) {
            credentialsCallback.onSuccess();
        } else {
            credentialsCallback.onFailed();
        }
    }

    public void login(String username, String password, Callbacks.credentialsCallback c) {
        credentialsCallback = c;

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

        url = String.format("https://api.vsa.lohl1kohl.de/validate?username=%s&password=%s", hashUsername, hashPassword);

        HttpGetRequest asyncTask = new HttpGetRequest(3000);

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
