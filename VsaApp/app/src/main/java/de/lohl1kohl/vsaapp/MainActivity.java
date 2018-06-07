package de.lohl1kohl.vsaapp;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private boolean showSettings = false;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

        return true;
    }

    public void displayView(int viewId) {

        Fragment fragment = null;
        SettingsFragment settingsFragment = null;
        String title = getString(R.string.app_name);

        // Get new fragment...
        switch (viewId) {
            case R.id.nav_vp:
                fragment = new VpFragment();
                title  = getString(R.string.vp);

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
            if (showSettings){
                // remove settings fragment...
                getFragmentManager().beginTransaction().
                        remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
                showSettings = false;
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        if (settingsFragment != null){
            if (!showSettings){
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }
}
