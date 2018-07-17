package de.lohl1kohl.vsaapp;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.CafetoriaHolder;
import de.lohl1kohl.vsaapp.holder.Callbacks.cafetoriaLoadedCallback;
import de.lohl1kohl.vsaapp.server.Cafetoria;
import de.lohl1kohl.vsaapp.server.Callbacks;


public class CafetoriaFragment extends BaseFragment {
    LinearLayout linearLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cafetoria, container, false);
        linearLayout = root.findViewById(R.id.cafetoria_list);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String id = settings.getString("pref_cafetoria_id", "-1");
        if (id.equals("-1")) {
            showCafetoriaLoginDialog();
        } else {
            setup();
        }
        return root;
    }

    void setup() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        cafetoriaLoadedCallback cafetoriaLoadedCallback = new cafetoriaLoadedCallback() {
            @Override
            public void onOldLoaded() {
                Log.i("Cafetoria", "Old loaded");
                displayMenues();
            }

            @Override
            public void onNewLoaded() {
                Log.i("Cafetoria", "New loaded");
                displayMenues();
            }

            @Override
            public void onConnectionFailed() {
                Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();
            }
        };
        CafetoriaHolder.load(mActivity, settings.getString("pref_cafetoria_id", "-1"), settings.getString("pref_cafetoria_pin", "-1"), cafetoriaLoadedCallback);
    }

    void displayMenues() {

    }

    public void showCafetoriaLoginDialog() {
        final Dialog loginDialog = new Dialog(mActivity);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(Objects.requireNonNull(loginDialog.getWindow()).getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_cafetoria);
        loginDialog.setCancelable(false);
        loginDialog.setTitle(R.string.cafetoriaDialog);

        final Button btn_login = loginDialog.findViewById(R.id.btn_cafetoriaOk);
        final EditText id = loginDialog.findViewById(R.id.cafetoria_id);
        final EditText pin = loginDialog.findViewById(R.id.cafetoria_password);
        final TextView feedback = loginDialog.findViewById(R.id.cafetoria_loginFeedback);

        btn_login.setOnClickListener(view -> {
            if (id.getText().toString().equals("")) {
                feedback.setText(R.string.no_id_set);
                return;
            }
            if (pin.getText().toString().equals("")) {
                feedback.setText(R.string.no_pin_set);
                return;
            }
            Callbacks.cafetoriaCallback cafetoriaCallback = new Callbacks.cafetoriaCallback() {
                @Override
                public void onReceived(String output) {
                    try {
                        JSONObject jsonObject = new JSONObject(output);
                        if (jsonObject.getString("error").equals("null")) {
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("pref_cafetoria_id", id.getText().toString());
                            editor.putString("pref_cafetoria_pin", pin.getText().toString());
                            editor.apply();
                            loginDialog.cancel();

                            Toast.makeText(mActivity, R.string.login_success, Toast.LENGTH_SHORT).show();
                            setup();
                        } else {
                            feedback.setText(R.string.cafetoriaDialog_statusFailed);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConnectionFailed() {
                    Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();
                }
            };
            new Cafetoria().updateMenues(id.getText().toString(), pin.getText().toString(), cafetoriaCallback);
        });
        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }
}