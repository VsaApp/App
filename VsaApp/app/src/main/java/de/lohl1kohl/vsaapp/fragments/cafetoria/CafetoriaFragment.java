package de.lohl1kohl.vsaapp.fragments.cafetoria;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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

import java.util.Calendar;
import java.util.Date;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.fragments.sp.LessonUtils;
import de.lohl1kohl.vsaapp.fragments.sp.SpDayAdapter;
import de.lohl1kohl.vsaapp.fragments.sp.SpHolder;
import de.lohl1kohl.vsaapp.loader.Callbacks;


public class CafetoriaFragment extends BaseFragment {

    View cafetoriaView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cafetoriaView = inflater.inflate(R.layout.fragment_cafetoria, container, false);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String id = settings.getString("pref_cafetoria_id", "-1");
        if (id.equals("-1")) {
            showCafetoriaLoginDialog();
        } else {
            loadMenues();
        }
        return cafetoriaView;
    }

    void displayMenues() {
        Log.i("cafetoria", String.valueOf(CafetoriaHolder.days));

        mActivity.runOnUiThread(() -> {
            ViewPager pager = cafetoriaView.findViewById(R.id.cafetoria_viewpager);
            CafetoriaDayAdapter adapter = new CafetoriaDayAdapter(mActivity, getFragmentManager());
            pager.setAdapter(adapter);
            TabLayout tabLayout = cafetoriaView.findViewById(R.id.cafetoria_tabs);
            tabLayout.setupWithViewPager(pager);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            try {
                if (weekday == -1 | weekday == 5) weekday = 0;
                else if (LessonUtils.isLessonPassed(SpHolder.getNumberOfLessons(weekday) - 1))
                    weekday++;
                TabLayout.Tab tab = tabLayout.getTabAt(weekday);
                try {
                    tab.select();
                } catch (Exception ignored) {

                }
            } catch (IndexOutOfBoundsException ignored) {

            }
        });
    }

    void loadMenues() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        Callbacks.baseLoadedCallback cafetoriaLoadedCallback = new Callbacks.baseLoadedCallback() {
            @Override
            public void onOldLoaded() {
                displayMenues();
            }

            @Override
            public void onNewLoaded() {
                displayMenues();
            }

            @Override
            public void onConnectionFailed() {
                displayMenues();
            }
        };
        CafetoriaHolder.load(mActivity, settings.getString("pref_cafetoria_id", "-1"), settings.getString("pref_cafetoria_pin", "-1"), cafetoriaLoadedCallback);
    }

    public void showCafetoriaLoginDialog() {
        final Dialog loginDialog = new Dialog(mActivity);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(loginDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_cafetoria);
        loginDialog.setCancelable(true);
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
            Callbacks.baseCallback cafetoriaCallback = new Callbacks.baseCallback() {
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
                            loadMenues();
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