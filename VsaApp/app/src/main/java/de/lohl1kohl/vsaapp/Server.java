package de.lohl1kohl.vsaapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Server {
    public Server (){

    }

    public boolean checkLoginData(String username, String password){
        MessageDigest digest;
        String url, hashPassword, hasUsername;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(username.getBytes());
            hasUsername = bytesToHexString(digest.digest());
            digest.update(password.getBytes());
            hashPassword = bytesToHexString((digest.digest()));
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return false;
        }
        url = String.format("https://vsa.lohl1kohl.de/validate?username=%s&password=%s", hasUsername, hashPassword);

        //TODO: Get msg of url and return true if everthing is correct
        return true;
    }

    // utility function
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
