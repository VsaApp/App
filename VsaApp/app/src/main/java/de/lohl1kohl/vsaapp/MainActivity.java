package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

        // Show the vpFragment as the start fragment...
        displayView(R.id.nav_sp);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            else finish();
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
            case R.id.nav_ags:
                navigationView.getMenu().getItem(4).setChecked(true);
                currentFragment = new AGsFragment();
                title = getString(R.string.ags);
                break;
            case R.id.nav_cafetoria:
                navigationView.getMenu().getItem(5).setChecked(true);
                currentFragment = new CafetoriaFragment();
                title = getString(R.string.cafetoria);
                break;
            case R.id.nav_documents:
                navigationView.getMenu().getItem(6).setChecked(true);
                currentFragment = new DocumentsFragment();
                title = getString(R.string.documents);
                break;
            case R.id.nav_web:
                navigationView.getMenu().getItem(7).setChecked(true);
                currentFragment = new WebFragment();
                title = getString(R.string.web);
                break;
            case R.id.nav_settings:
                navigationView.getMenu().getItem(8).setChecked(true);
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
                        remove(getSupportFragmentManager().findFragmentById(R.id.content_frame)).commit();
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
        notificationManager.cancelAll();
    }
}
