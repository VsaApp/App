package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.Callbacks.spLoadedCallback;
import de.lohl1kohl.vsaapp.holder.Callbacks.vpLoadedCallback;
import de.lohl1kohl.vsaapp.holder.DatesHolder;
import de.lohl1kohl.vsaapp.holder.SpHolder;
import de.lohl1kohl.vsaapp.holder.SubjectSymbolsHolder;
import de.lohl1kohl.vsaapp.holder.TeacherHolder;
import de.lohl1kohl.vsaapp.holder.VpHolder;
import de.lohl1kohl.vsaapp.server.Callbacks;
import de.lohl1kohl.vsaapp.server.Login;

import static de.lohl1kohl.vsaapp.WebFragment.pushChoices;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final MainActivity mainActivity = this;
    private boolean showSettings = false;
    private int currentNavId = 0;
    private Fragment currentFragment;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LessonUtils.setWeekdays(new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.weekdays))));

        // Init teacherHolder...
        new Thread(() -> TeacherHolder.load(this, true)).start();

        // Init dates holder...
        new Thread(() -> DatesHolder.load(this)).start();

        // Init subjectSymbolsHolder...
        new Thread(() -> SubjectSymbolsHolder.load(this)).start();

        vpLoadedCallback vpLoadedCallback = new vpLoadedCallback() {
            @Override
            public void onFinished() {
                SpHolder.load(mainActivity, false);
                if (MainActivity.this.getIntent().getStringExtra("day") != null) {
                    VpFragment.selectDay(MainActivity.this.getIntent().getStringExtra("day"));
                }
                try {
                    pushChoices(mainActivity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed() {
                if (MainActivity.this.getIntent().getStringExtra("day") != null) {
                    VpFragment.selectDay(MainActivity.this.getIntent().getStringExtra("day"));
                }
            }
        };

        new Thread(() -> {
            spLoadedCallback spLoadedCallback = new spLoadedCallback() {
                @Override
                public void onOldLoaded() {
                    new Thread(() -> VpHolder.load(mainActivity, vpLoadedCallback)).start();
                }

                @Override
                public void onNewLoaded() {
                    new Thread(() -> VpHolder.load(mainActivity, vpLoadedCallback)).start();
                }

                @Override
                public void onConnectionFailed() {

                }

                @Override
                public void onNoSp() {

                }
            };
            SpHolder.load(this, true, spLoadedCallback);
        }).start();

        // Show the vpFragment as the start fragment...
        displayView(R.id.nav_sp);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        new Thread(() -> {
            // Check the login data...
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String username = sharedPref.getString("pref_username", "-1");
            String password = sharedPref.getString("pref_password", "-1");
            if (username.equals("-1") || password.equals("-1")) {
                runOnUiThread(this::showLoginScreen);
            } else {
                Callbacks.credentialsCallback callback = new Callbacks.credentialsCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("VsaApp/Server", "Password success");
                    }

                    @Override
                    public void onFailed() {
                        Log.e("VsaApp/Server", "Password failed");
                        runOnUiThread(() -> showLoginScreen());
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.e("VsaApp/Server", "No connection");
                        runOnUiThread(() -> Toast.makeText(mainActivity, R.string.no_connection, Toast.LENGTH_LONG).show());
                    }
                };
                new Login().login(username, password, callback);
            }
        }).start();
        if (getIntent().getStringExtra("page") != null) {
            displayView(R.id.nav_vp);
            navigationView.getMenu().getItem(1).setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentNavId != R.id.nav_sp) displayView(R.id.nav_sp);
            else super.onBackPressed();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String grade = sharedPref.getString("pref_grade", "-1");
        if (!grade.equals("-1")) {
            ((TextView) findViewById(R.id.header_name)).setText(getString(R.string.app_name) + " - " + grade);
        }
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_home || super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void showLoginScreen() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        final Dialog loginDialog = new Dialog(MainActivity.this);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(Objects.requireNonNull(loginDialog.getWindow()).getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
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
            Callbacks.credentialsCallback callback = new Callbacks.credentialsCallback() {
                @Override
                public void onSuccess() {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("pref_username", username.getText().toString());
                    editor.putString("pref_password", password.getText().toString());
                    editor.putString("pref_grade", grades[w[0]]);
                    FirebaseHandler.subscribe(MainActivity.this, grades[w[0]]);
                    ((TextView) findViewById(R.id.header_name)).setText(getString(R.string.app_name) + " - " + grades[w[0]]);
                    editor.apply();
                    loginDialog.cancel();
                    ((SpFragment) currentFragment).syncSp();
                    Toast.makeText(mainActivity, R.string.login_success, Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Get the item id...
        int id = item.getItemId();

        // Set new fragment...
        displayView(id);

        // Close the navigator...
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @SuppressWarnings("deprecation")
    private void displayView(int viewId) {

        // If the logindata is correct, open fragment...
        SettingsFragment settingsFragment = null;
        String title = getString(R.string.app_name);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Get new fragment...
        switch (viewId) {
            case R.id.nav_sp:
                navigationView.getMenu().getItem(0).setChecked(true);
                currentFragment = new SpFragment();
                title = getString(R.string.sp);
                break;
            case R.id.nav_vp:
                navigationView.getMenu().getItem(1).setChecked(true);
                currentFragment = new VpFragment();
                title = getString(R.string.vp);
                break;
            case R.id.nav_teacher:
                navigationView.getMenu().getItem(2).setChecked(true);
                currentFragment = new TeacherFragment();
                title = getString(R.string.teacher);
                break;
            case R.id.nav_dates:
                navigationView.getMenu().getItem(3).setChecked(true);
                currentFragment = new DatesFragment();
                title = getString(R.string.menu_dates);
                break;
            case R.id.nav_cafetoria:
                navigationView.getMenu().getItem(4).setChecked(true);
                currentFragment = new CafetoriaFragment();
                title = getString(R.string.cafetoria);
                break;
            case R.id.nav_web:
                navigationView.getMenu().getItem(5).setChecked(true);
                currentFragment = new WebFragment();
                title = getString(R.string.web);
                break;
            case R.id.nav_settings:
                navigationView.getMenu().getItem(6).setChecked(true);
                currentFragment.onDestroy();
                settingsFragment = new SettingsFragment();
                break;
        }

        // If the new fragment is the same like the new break up...
        if (viewId == currentNavId) {
            return;
        }

        // Set new fragment...
        if (currentFragment != null) {
            if (showSettings) {
                // remove settings fragment...
                getFragmentManager().beginTransaction().
                        remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
                showSettings = false;
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, currentFragment);
            ft.commit();
        }

        // set the toolbar title...
        if (getSupportActionBar() != null && !showSettings) {
            getSupportActionBar().setTitle(title);
        }

        if (settingsFragment != null) {
            if (!showSettings) {
                // remove current fragment...
                getSupportFragmentManager().beginTransaction().
                        remove(Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.content_frame))).commit();
                showSettings = true;

                // Add settings fragment...
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, settingsFragment)
                        .commit();
            }
        }

        currentNavId = viewId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).cancelAll();
    }
}
