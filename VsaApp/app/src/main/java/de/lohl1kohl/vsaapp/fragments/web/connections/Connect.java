package de.lohl1kohl.vsaapp.fragments.web.connections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.lohl1kohl.vsaapp.loader.BaseLoader;
import de.lohl1kohl.vsaapp.loader.Callbacks;

public class Connect extends BaseLoader {

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

    @SuppressLint("HardwareIds")
    private String getMyID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void connect(Context context, String id, Callbacks.baseCallback c) {
        TAG = "Web";
        url = "https://vsa.lohl1kohl.de/connect?client=%s&web=%s&grade=%s&username=%s&password=%s";

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

        url = String.format(url, getMyID(context), id, grade, hashUsername, hashPassword);

        this.get(c);
    }
}
