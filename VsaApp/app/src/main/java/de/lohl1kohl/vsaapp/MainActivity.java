package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private boolean showSettings = false;
    private final Server server = new Server();
    private final MainActivity mainActivity = this;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the vpFragment as the start fragment...
        displayView(R.id.nav_sp);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        // Check the login data...
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPref.getString("pref_username", "-1");
        String password = sharedPref.getString("pref_password", "-1");
        if (username.equals("-1") || password.equals("-1")) {
            showLoginScreen();
        } else {
            Server.credentialsCallback callback = new Server.credentialsCallback() {
                @Override
                public void onSuccess() {
                    Log.i("VsaApp/Server", "Password success");
                }

                @Override
                public void onFailed() {
                    Log.e("VsaApp/Server", "Password failed");
                    showLoginScreen();
                }

                @Override
                public void onConnectionFailed(){
                    Log.e("VsaApp/Server", "No connection");
                    Toast.makeText(mainActivity, R.string.no_connection, Toast.LENGTH_LONG).show();
                }
            };
            server.login(username, password, callback);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        final Dialog loginDialog = new Dialog(this);
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
        final EditText password = loginDialog.findViewById(R.id.login_passwort);
        final TextView feedback = loginDialog.findViewById(R.id.lbl_loginFeedback);

        String[] grades = getResources().getStringArray(R.array.nameOfGrades);
        final int[] w = {0};

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
            Server.credentialsCallback callback = new Server.credentialsCallback() {
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
                    Toast.makeText(mainActivity, R.string.login_success, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed() {
                    feedback.setText(R.string.loginDialog_statusFailed);
                }

                @Override
                public void onConnectionFailed() {feedback.setText(R.string.no_connection);}
            };
            server.login(username.getText().toString(), password.getText().toString(), callback);
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
        return true;
    }

    private void displayView(int viewId) {

        // If the logindata is correct, open fragment...
        Fragment fragment = null;
        SettingsFragment settingsFragment = null;
        String title = getString(R.string.app_name);

        // Get new fragment...
        switch (viewId) {
            case R.id.nav_vp:
                fragment = new VpFragment();
                title = getString(R.string.vp);

                break;
            case R.id.nav_sp:
                fragment = new SpFragment();
                title = getString(R.string.sp);
                break;

            case R.id.nav_teacher:
                fragment = new TeacherFragment();
                title = getString(R.string.teacher);
                break;

            case R.id.nav_settings:
                settingsFragment = new SettingsFragment();
        }

        // Set new fragment...
        if (fragment != null) {
            if (showSettings) {
                // remove settings fragment...
                getFragmentManager().beginTransaction().
                        remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
                showSettings = false;
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        if (settingsFragment != null) {
            if (!showSettings) {
                // remove current fragment...
                getSupportFragmentManager().beginTransaction().
                        remove(getSupportFragmentManager().findFragmentById(R.id.content_frame)).commit();
                showSettings = true;
            }

            // Add settings fragment...
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new SettingsFragment())
                    .commit();
        }

        // set the toolbar title...
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
