package de.lohl1kohl.vsaapp.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.lohl1kohl.vsaapp.Document;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.server.Callbacks.documentsCallback;
import de.lohl1kohl.vsaapp.server.Documents;

public class DocumentsHolder {
    @SuppressLint("StaticFieldLeak")
    private static List<Document> documents = new ArrayList<>();

    public static void load(Context context) {
        load(context, null);
    }

    public static void load(Context context, Callbacks.documentsLoadedCallback documentsLoadedCallback) {
        List<Document> savedDocuments = getSavedDocuments(context);
        if (savedDocuments != null) {
            documents = savedDocuments;
            if (documentsLoadedCallback != null) documentsLoadedCallback.onOldLoaded();
        }
        documentsCallback documentsCallback = new documentsCallback() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onReceived(String output) {
                DocumentsHolder.documents = convertJsonToArray(output);
                Log.v("VsaApp/Server", "Success");
                if (documentsLoadedCallback != null) documentsLoadedCallback.onNewLoaded();
            }

            @Override
            public void onConnectionFailed() {
                Log.e("VsaApp/Server", "Failed");
                Toast.makeText(context, R.string.no_connection, Toast.LENGTH_SHORT).show();
                if (documentsLoadedCallback != null)
                    documentsLoadedCallback.onConnectionFailed();
            }
        };
        new Documents().getDocuments(documentsCallback);
    }

    public static List<Document> searchDocuments(String str) {
        str = str.toLowerCase();
        List<Document> documents = new ArrayList<>();
        for (Document document : DocumentsHolder.documents) {
            if (document.getText().toLowerCase().contains(str)) {
                documents.add(document);
            } else if (document.getGroupName().toLowerCase().contains(str)) {
                documents.add(document);
            }
        }
        return documents;
    }

    private static List<Document> convertJsonToArray(String array) {
        List<Document> documents = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(array);
            JSONObject groups = jsonObject.getJSONObject("groups");
            JSONArray ds = jsonObject.getJSONArray("documents");
            for (int i = 0; i < ds.length(); i++) {
                JSONObject d = ds.getJSONObject(i);
                documents.add(new Document(d.getString("url"), d.getString("text"), d.getInt("group"), groups.getString(String.valueOf(d.getInt("group")))));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return documents;
    }

    private static List<Document> getSavedDocuments(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedDocuments = sharedPref.getString("pref_documents", "-1");

        Log.i("Documents", savedDocuments);

        if (savedDocuments.equals("-1")) return null;
        return convertJsonToArray(savedDocuments);
    }

    public static List<Document> getDocuments() {
        return documents;
    }
}
