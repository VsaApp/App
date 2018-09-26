package de.lohl1kohl.vsaapp.fragments.documents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.holders.DocumentsHolder;


public class DocumentsFragment extends BaseFragment {
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout list;
    private List<Thread> threads = new ArrayList<>();

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void listDocuments(List<Document> documents) {
        int viewsCount = 0;
        mActivity.runOnUiThread(() -> list.removeAllViews());
        for (Document document : documents) {
            View v = LayoutInflater.from(mActivity).inflate(R.layout.document_item, null);
            ((TextView) v.findViewById(R.id.documentText)).setText(document.getText());
            ((TextView) v.findViewById(R.id.documentGroupName)).setText(document.getGroupName());
            Uri uri = Uri.parse("https://docs.google.com/viewer?url=" + URLEncoder.encode(document.getUrl()));
            v.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, uri)));
            mActivity.runOnUiThread(() -> list.addView(v));
            viewsCount++;
        }
        if (viewsCount == 0) {
            mActivity.runOnUiThread(() -> list.addView(LayoutInflater.from(mActivity).inflate(R.layout.no_document, null)));
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_documents, container, false);
        EditText search = root.findViewById(R.id.documentSearch);
        list = root.findViewById(R.id.documentList);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                threads.add(new Thread(() -> listDocuments(DocumentsHolder.searchDocuments(s.toString()))));
            }
        });
        new Thread(() -> listDocuments(DocumentsHolder.getDocuments())).start();
        new Thread(() -> {
            while (true) {
                if (threads.size() != 0) {
                    threads.get(0).start();
                    try {
                        threads.get(0).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    threads.remove(0);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return root;
    }
}