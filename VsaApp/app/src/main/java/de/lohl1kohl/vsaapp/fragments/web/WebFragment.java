package de.lohl1kohl.vsaapp.fragments.web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.lohl1kohl.vsaapp.LoadingActivity;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.StringUtils;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.fragments.sp.Lesson;
import de.lohl1kohl.vsaapp.fragments.sp.SpHolder;
import de.lohl1kohl.vsaapp.fragments.sp.Subject;
import de.lohl1kohl.vsaapp.fragments.web.connections.Connect;
import de.lohl1kohl.vsaapp.fragments.web.connections.Delete;
import de.lohl1kohl.vsaapp.loader.Callbacks;

import static android.support.v4.content.ContextCompat.checkSelfPermission;


public class WebFragment extends BaseFragment {

    private static final int REQUEST_CODE_QR_SCAN = 101;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 102;
    private static final String CAMERA_PERMISSION = "android.permission.CAMERA";
    private View root;
    private LinearLayout list;

    public static void pushChoices(Context context) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int weekday = 0; weekday < 5; weekday++) {
            int lessons = SpHolder.getNumberOfLessons(weekday);
            for (int l = 0; l < lessons; l++) {
                Lesson lesson = SpHolder.getLesson(weekday, l);
                if (lesson.numberOfSubjects() > 1) {
                    Subject subject = lesson.getSubject();
                    jsonArray.put(new JSONObject().put("weekday", subject.day).put("unit", subject.unit).put("subject", subject.name).put("teacher", StringUtils.poop(subject.teacher)));
                }
            }
        }
        Callbacks.baseCallback pushCallback = new Callbacks.baseCallback() {
            @Override
            public void onReceived(String output) {

            }

            @Override
            public void onConnectionFailed() {
                ((LoadingActivity) context).runOnUiThread(() -> Toast.makeText(context, R.string.no_connection, Toast.LENGTH_LONG).show());
            }
        };
        new Push().push(context, jsonArray, pushCallback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_web, container, false);
        if (getPermission()) {
            gotPermission();
        }
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_result");
            Callbacks.baseCallback connectCallback = new Callbacks.baseCallback() {
                @Override
                public void onReceived(String output) {
                    try {
                        if (new JSONObject(output).getString("error").equals("null")) {
                            pushChoices(mActivity);
                            Thread.sleep(1000);
                            listConnections();
                        }
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConnectionFailed() {
                    mActivity.runOnUiThread(() -> Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_LONG).show());
                }
            };
            Connect connect = new Connect();
            connect.connect(mActivity, result, connectCallback);
        }
    }

    @SuppressLint("InflateParams")
    public void listConnections() {
        Callbacks.baseCallback connectionsCallback = new Callbacks.baseCallback() {
            @Override
            public void onReceived(String output) {
                List<RelativeLayout> views = new ArrayList<>();
                LayoutInflater layoutinflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                try {
                    JSONObject jsonObject = new JSONObject(output);
                    if (jsonObject.getString("error").equals("null")) {
                        JSONArray connections = jsonObject.getJSONArray("connections");
                        for (int i = 0; i < connections.length(); i++) {
                            JSONObject connection = connections.getJSONObject(i);
                            RelativeLayout view = (RelativeLayout) layoutinflater.inflate(R.layout.web_cell, null);
                            TextView id = view.findViewById(R.id.web_cell_id);
                            id.setText(connection.getString("id"));
                            TextView time = view.findViewById(R.id.web_cell_time);
                            time.setText(connection.getString("time"));
                            ImageButton delete = view.findViewById(R.id.web_cell_delete);
                            delete.setOnClickListener(view1 -> {
                                Callbacks.baseCallback deleteCallback = new Callbacks.baseCallback() {
                                    @Override
                                    public void onReceived(String output1) {
                                        listConnections();
                                    }

                                    @Override
                                    public void onConnectionFailed() {
                                        mActivity.runOnUiThread(() -> Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_LONG).show());
                                    }
                                };
                                try {
                                    new Delete().delete(connection.getString("id"), deleteCallback);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            views.add(view);
                        }
                        mActivity.runOnUiThread(() -> list.removeAllViews());
                        mActivity.runOnUiThread(() -> {
                            for (RelativeLayout ll : views) {
                                list.addView(ll);
                            }
                            try {
                                list.getChildAt(list.getChildCount() - 1).findViewById(R.id.line).setVisibility(View.GONE);
                            } catch (NullPointerException ignored) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed() {
                mActivity.runOnUiThread(() -> Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_LONG).show());
            }
        };
        de.lohl1kohl.vsaapp.fragments.web.connections.List list = new de.lohl1kohl.vsaapp.fragments.web.connections.List();
        list.list(mActivity, connectionsCallback);
    }

    public void scan() {
        Intent i = new Intent(mActivity, QrCodeActivity.class);
        i.putExtra("vibrate", true);
        i.putExtra("showcorners", false);
        i.putExtra("showlaser", false);
        i.putExtra("showtext", false);
        i.putExtra("showheader", false);
        i.putExtra("showflashlight", false);
        i.putExtra("allowbackpress", true);
        startActivityForResult(i, REQUEST_CODE_QR_SCAN);
    }

    public void gotPermission() {
        Button connect = root.findViewById(R.id.web_connect);
        connect.setOnClickListener(view -> scan());
        list = root.findViewById(R.id.web_connections_list);
        listConnections();
    }

    public boolean getPermission() {
        if (checkSelfPermission(mActivity, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{CAMERA_PERMISSION}, REQUEST_CODE_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA_PERMISSION: {
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    getPermission();
                } else {
                    gotPermission();
                }
            }
        }
    }
}