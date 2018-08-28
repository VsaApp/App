package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

import de.lohl1kohl.vsaapp.holder.AGsHolder;
import de.lohl1kohl.vsaapp.holder.CafetoriaHolder;
import de.lohl1kohl.vsaapp.holder.Callbacks;
import de.lohl1kohl.vsaapp.holder.DatesHolder;
import de.lohl1kohl.vsaapp.holder.DocumentsHolder;
import de.lohl1kohl.vsaapp.holder.SpHolder;
import de.lohl1kohl.vsaapp.holder.SubjectSymbolsHolder;
import de.lohl1kohl.vsaapp.holder.TeacherHolder;
import de.lohl1kohl.vsaapp.holder.VpHolder;
import de.lohl1kohl.vsaapp.server.Login;

import static de.lohl1kohl.vsaapp.WebFragment.pushChoices;

public class LoadingActivity extends AppCompatActivity {

    private int holders = 8;
    private int loaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        new Thread(() -> {
            // Check the login data...
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String username = sharedPref.getString("pref_username", "-1");
            String password = sharedPref.getString("pref_password", "-1");
            if (username.equals("-1") || password.equals("-1")) {
                runOnUiThread(this::showLoginScreen);
            } else {
                de.lohl1kohl.vsaapp.server.Callbacks.credentialsCallback callback = new de.lohl1kohl.vsaapp.server.Callbacks.credentialsCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("VsaApp/Server", "Password success");
                        checkFinish();
                        loadAll();
                    }

                    @Override
                    public void onFailed() {
                        Log.e("VsaApp/Server", "Password failed");
                        runOnUiThread(() -> showLoginScreen());
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.e("VsaApp/Server", "No connection");
                        runOnUiThread(() -> Toast.makeText(LoadingActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show());
                        checkFinish();
                        loadAll();
                    }
                };
                new Login().login(username, password, callback);
            }
        }).start();
    }

    private void loadAll() {
        LessonUtils.setWeekdays(new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.weekdays))));

        // Init teacherHolder...
        new Thread(() -> TeacherHolder.load(this, true, new Callbacks.teachersLoadedCallback() {
            @Override
            public void onOldLoaded() {

            }

            @Override
            public void onNewLoaded() {
                checkFinish();
            }

            @Override
            public void onConnectionFailed() {
                checkFinish();
            }
        })).start();

        // Init dates holder...
        new Thread(() -> DatesHolder.load(this, new Callbacks.datesLoadedCallback() {
            @Override
            public void onOldLoaded() {

            }

            @Override
            public void onNewLoaded() {
                checkFinish();
            }

            @Override
            public void onConnectionFailed() {
                checkFinish();
            }
        })).start();

        // Init subjectSymbolsHolder...
        new Thread(() -> {
            SubjectSymbolsHolder.load(this);
            checkFinish();
        }).start();

        // Init documentsHolder...
        new Thread(() -> DocumentsHolder.load(this, new Callbacks.documentsLoadedCallback() {
            @Override
            public void onOldLoaded() {

            }

            @Override
            public void onNewLoaded() {
                checkFinish();
            }

            @Override
            public void onConnectionFailed() {
                checkFinish();
            }
        })).start();

        new Thread(() -> {
            Callbacks.vpLoadedCallback vpLoadedCallback = new Callbacks.vpLoadedCallback() {
                @Override
                public void onOldLoaded() {

                }

                @Override
                public void onNewLoaded() {
                    SpHolder.load(LoadingActivity.this, false);
                    if (LoadingActivity.this.getIntent().getStringExtra("day") != null) {
                        VpFragment.selectDay(LoadingActivity.this.getIntent().getStringExtra("day"));
                    }
                    try {
                        pushChoices(LoadingActivity.this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    checkFinish();
                }

                @Override
                public void onConnectionFailed() {
                    if (LoadingActivity.this.getIntent().getStringExtra("day") != null) {
                        VpFragment.selectDay(LoadingActivity.this.getIntent().getStringExtra("day"));
                    }
                    checkFinish();
                }
            };

            Callbacks.spLoadedCallback spLoadedCallback = new Callbacks.spLoadedCallback() {
                @Override
                public void onOldLoaded() {
                    new Thread(() -> VpHolder.load(LoadingActivity.this, vpLoadedCallback)).start();
                }

                @Override
                public void onNewLoaded() {
                    new Thread(() -> VpHolder.load(LoadingActivity.this, vpLoadedCallback)).start();
                    checkFinish();
                }

                @Override
                public void onConnectionFailed() {
                    checkFinish();
                }

                @Override
                public void onNoSp() {

                }
            };
            SpHolder.load(this, true, spLoadedCallback);
        }).start();

        new Thread(() -> {
            Callbacks.agsLoadedCallback agsLoadedCallback = new Callbacks.agsLoadedCallback() {
                @Override
                public void onOldLoaded() {

                }

                @Override
                public void onNewLoaded() {
                    checkFinish();
                }

                @Override
                public void onConnectionFailed() {
                    checkFinish();
                }
            };
            AGsHolder.load(LoadingActivity.this, agsLoadedCallback);
        }).start();

        new Thread(() -> {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoadingActivity.this);
            Callbacks.cafetoriaLoadedCallback cafetoriaLoadedCallback = new Callbacks.cafetoriaLoadedCallback() {
                @Override
                public void onOldLoaded() {

                }

                @Override
                public void onNewLoaded() {
                    checkFinish();
                }

                @Override
                public void onConnectionFailed() {
                    checkFinish();
                }
            };
            CafetoriaHolder.load(LoadingActivity.this, settings.getString("pref_cafetoria_id", "-1"), settings.getString("pref_cafetoria_pin", "-1"), cafetoriaLoadedCallback);
        });
    }

    @SuppressLint("SetTextI18n")
    private void showLoginScreen() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        final Dialog loginDialog = new Dialog(LoadingActivity.this);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(loginDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_login);
        loginDialog.setCancelable(false);
        loginDialog.setTitle(R.string.loginDialog);

        final Button btn_login = loginDialog.findViewById(R.id.btn_loginOk);
        final Button btn_grade = loginDialog.findViewById(R.id.btn_loginGrade);
        final EditText username = loginDialog.findViewById(R.id.login_username);
        final EditText password = loginDialog.findViewById(R.id.login_password);
        final TextView feedback = loginDialog.findViewById(R.id.lbl_loginFeedback);

        String[] grades = getResources().getStringArray(R.array.nameOfGrades);
        final int[] w = {-1};

        btn_grade.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.choose_grade));
            builder.setSingleChoiceItems(grades, 1, (dialog, which) -> {
                w[0] = which;
                dialog.cancel();
                btn_grade.setText(getString(R.string.choose_grade) + " - " + grades[w[0]]);
            });

            builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();

        });

        btn_login.setOnClickListener(view -> {
            if (username.getText().toString().equals("")) {
                feedback.setText(R.string.no_username_set);
                return;
            }
            if (password.getText().toString().equals("")) {
                feedback.setText(R.string.no_password_set);
                return;
            }
            if (w[0] == -1) {
                feedback.setText(R.string.no_class);
                return;
            }
            de.lohl1kohl.vsaapp.server.Callbacks.credentialsCallback callback = new de.lohl1kohl.vsaapp.server.Callbacks.credentialsCallback() {
                @Override
                public void onSuccess() {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoadingActivity.this);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("pref_username", username.getText().toString());
                    editor.putString("pref_password", password.getText().toString());
                    editor.putString("pref_grade", grades[w[0]]);
                    FirebaseHandler.subscribe(LoadingActivity.this, grades[w[0]]);
                    ((TextView) findViewById(R.id.header_name)).setText(getString(R.string.app_name) + " - " + grades[w[0]]);
                    editor.apply();
                    loginDialog.cancel();
                    Toast.makeText(LoadingActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    checkFinish();
                    loadAll();
                }

                @Override
                public void onFailed() {
                    feedback.setText(R.string.loginDialog_statusFailed);
                }

                @Override
                public void onConnectionFailed() {
                    feedback.setText(R.string.no_connection);
                }
            };
            new Login().login(username.getText().toString(), password.getText().toString(), callback);
        });
        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }

    @SuppressLint("SetTextI18n")
    private void checkFinish() {
        loaded++;
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress((loaded / holders) * 100);
        TextView progressText = findViewById(R.id.progressText);
        runOnUiThread(() -> progressText.setText(String.valueOf((loaded / holders) * 100) + " %"));
        if (loaded == holders) {
            Intent intent = new Intent(this, MainActivity.class);
            if (getIntent().getStringExtra("page") != null) {
                intent.putExtra("page", getIntent().getStringExtra("page"));
            }
            startActivity(intent);
            finish();
        }
    }
}
