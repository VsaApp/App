package de.lohl1kohl.vsaapp;

import android.os.AsyncTask;
import de.lohl1kohl.vsaapp.loader.AsyncResponse;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRequest extends AsyncTask<String, Void, String> {
    private static final String REQUEST_METHOD = "GET";
    private static final int CONNECTION_TIMEOUT = 3000;
    private static int READ_TIMEOUT = 3000;
    public AsyncResponse delegate = null;

    public HttpGetRequest(int timeout) {
        READ_TIMEOUT = timeout;
    }

    @Override
    protected String doInBackground(String... params) {
        String stringUrl = params[0];
        String result;
        String inputLine;
        HttpURLConnection connection = null;
        try {
            //Create a URL object holding our url
            URL myUrl = new URL(stringUrl);
            //Create a connection
            connection = (HttpURLConnection) myUrl.openConnection();
            //Enable authentication
            connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64((LoadingActivity.username + ":" + LoadingActivity.password).getBytes())));
            //Set methods and timeouts
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            //Connect to our url
            connection.connect();
            //Create a new InputStreamReader
            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());
            //Create a new buffered reader and String Builder
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            //Check if the line we are reading is not null
            while ((inputLine = reader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            //Close our InputStream and Buffered reader
            reader.close();
            streamReader.close();
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
            if (connection != null) {
                try {
                    if (connection.getResponseCode() == 404) {
                        result = "404";
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);

    }
}
