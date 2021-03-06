package de.lohl1kohl.vsaapp.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.lohl1kohl.vsaapp.fragments.documents.Document;
import de.lohl1kohl.vsaapp.fragments.documents.Documents;
import de.lohl1kohl.vsaapp.loader.Callbacks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DocumentsHolder {
    @SuppressLint("StaticFieldLeak")
    private static List<Document> documents = new ArrayList<>();

    public static void load(Context context, boolean update) {
        load(context, update, null);
    }

    public static void load(Context context, boolean update, Callbacks.baseLoadedCallback documentsLoadedCallback) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        List<Document> savedDocuments = getSavedDocuments(context);

        if (update) {
            Callbacks.baseCallback documentsCallback = new Callbacks.baseCallback() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onReceived(String output) {
                    documents = convertJsonToArray(output);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("pref_documents", output);
                    editor.apply();
                    if (documentsLoadedCallback != null) documentsLoadedCallback.onLoaded();
                }

                @Override
                public void onConnectionFailed() {
                    if (savedDocuments != null) {
                        documents = savedDocuments;
                    }

                    if (documentsLoadedCallback != null)
                        documentsLoadedCallback.onLoaded();
                }
            };
            new Documents().getDocuments(documentsCallback);
        } else {
            if (savedDocuments != null) {
                documents = savedDocuments;
            }
            if (documentsLoadedCallback != null)
                documentsLoadedCallback.onLoaded();
        }
    }

    public static boolean isLoaded() {
        return documents.size() > 0;
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

        if (savedDocuments.equals("-1")) return null;
        return convertJsonToArray(savedDocuments);
    }

    public static List<Document> getDocuments() {
        return documents;
    }
}
