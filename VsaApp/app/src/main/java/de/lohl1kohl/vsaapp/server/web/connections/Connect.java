package de.lohl1kohl.vsaapp.server.web.connections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.lohl1kohl.vsaapp.AsyncResponse;
import de.lohl1kohl.vsaapp.HttpGetRequest;
import de.lohl1kohl.vsaapp.server.Callbacks;

public class Connect implements AsyncResponse {

    private Callbacks.connectCallback connectCallback;

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
            connectCallback.onConnectionFailed();
        } else {
            connectCallback.onReceived(output);
        }
    }

    @SuppressLint("HardwareIds")
    private String getMyID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void connect(Context context, String id, Callbacks.connectCallback c) {
        connectCallback = c;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String grade = sharedPref.getString("pref_grade", "-1");
        String username = sharedPref.getString("pref_username", "-1");
        String password = sharedPref.getString("pref_password", "-1");
        MessageDigest digest;
        String hashPassword = "";
        String hashUsername = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(username.getBytes());
            hashUsername = bytesToHexString(digest.digest());
            digest.update(password.getBytes());
            hashPassword = bytesToHexString((digest.digest()));
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        String url = String.format("https://vsa.lohl1kohl.de/connect?client=%s&web=%s&grade=%s&username=%s&password=%s", getMyID(context), id, grade, hashUsername, hashPassword);
        Log.i("VsaApp/Server/Web", "Open: " + url);

        HttpGetRequest asyncTask = new HttpGetRequest();

        //this to set delegate/listener back to this class
        asyncTask.delegate = this;

        //execute the async task
        asyncTask.execute(url);
    }
}
