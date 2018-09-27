package de.lohl1kohl.vsaapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.lohl1kohl.vsaapp.holders.AGsHolder;
import de.lohl1kohl.vsaapp.holders.DatesHolder;
import de.lohl1kohl.vsaapp.holders.DocumentsHolder;
import de.lohl1kohl.vsaapp.holders.SubjectSymbolsHolder;
import de.lohl1kohl.vsaapp.holders.VpHolder;
import de.lohl1kohl.vsaapp.holders.SpHolder;
import de.lohl1kohl.vsaapp.holders.TeacherHolder;

import de.lohl1kohl.vsaapp.fragments.sp.Lesson;
import de.lohl1kohl.vsaapp.fragments.sp.LessonUtils;
import de.lohl1kohl.vsaapp.fragments.vp.VpFragment;

import de.lohl1kohl.vsaapp.jobs.JobCreator;
import de.lohl1kohl.vsaapp.jobs.StartJob;

import de.lohl1kohl.vsaapp.loader.Callbacks;
import de.lohl1kohl.vsaapp.holders.SumsHolder;

import static de.lohl1kohl.vsaapp.fragments.web.WebFragment.pushChoices;

public class LoadingActivity extends AppCompatActivity {

    private int holders = 8;
    private int loaded = 0;
    private Map<String, Boolean> sums;
    private boolean visible = true;

    @Override
    public void onResume() {
        super.onResume();
        visible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        visible = false;
    }

    public static void createJob(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean("pref_mutePhone", true)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int s;
            if (!isInSchool()) {
                final AudioManager mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                editor.putInt("ringer_mode", mode.getRingerMode());
                editor.apply();
                int start = secondsUntilStart(getNextStartTime()) * 1000;
                s = new JobRequest.Builder(StartJob.TAG)
                        .setExact(start)
                        .setUpdateCurrent(true)
                        .build()
                        .schedule();
            } else {
                s = new JobRequest.Builder(StartJob.TAG)
                        .setUpdateCurrent(true)
                        .startNow()
                        .build()
                        .schedule();
            }
            editor.putInt("startid", s);
        }
    }

    private static boolean isInSchool() {
        Calendar now = Calendar.getInstance();
        now.setTime(new java.util.Date());

        Calendar start = Calendar.getInstance();
        start.setTime(new java.util.Date());
        start.set(Calendar.HOUR_OF_DAY, 7);
        start.set(Calendar.MINUTE, 50);
        start.set(Calendar.SECOND, 0);

        Calendar end = Calendar.getInstance();
        end.setTime(new java.util.Date());
        end.set(Calendar.HOUR_OF_DAY, 7);
        end.set(Calendar.MINUTE, 50);
        end.set(Calendar.SECOND, 0);
        if (end.get(Calendar.DAY_OF_WEEK) == 1 || end.get(Calendar.DAY_OF_WEEK) == 7) return false;
        List<Lesson> lessons = SpHolder.getDay(end.get(Calendar.DAY_OF_WEEK) - 2);
        end.add(Calendar.MINUTE, LessonUtils.endTimes[lessons.size() - 1] + 20); // End time of last lesson + 10 minutes ( + 10 minutes for 7:50am to 8:00am)

        return now.after(start) && now.before(end);
    }

    private static java.util.Date getNextStartTime() {
        java.util.Date now = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 50);
        cal.set(Calendar.SECOND, 0);
        if (now.after(cal.getTime())) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) cal.add(Calendar.DAY_OF_YEAR, 2);
        else if (cal.get(Calendar.DAY_OF_WEEK) == 7) cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }

    private static int secondsUntilStart(java.util.Date start) {
        final long millis = start.getTime() - System.currentTimeMillis();
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return (int) (hours * 3600 + minutes * 60 + seconds);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        JobManager.create(this).addJobCreator(new JobCreator());

        startLoading();

    }

    private void startLoading(){
        /*
            Structure:
            - test login
            - get sums
            - load all Holder (this::loadAll)
         */
        loaded = 0;

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
                        updateStatus();
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
                        updateStatus();
                        loadAll();
                    }
                };
                new Login().login(username, password, callback);
            }
        }).start();
    }

    private void noConnection(){
        runOnUiThread(() -> Toast.makeText(LoadingActivity.this, R.string.need_connection, Toast.LENGTH_LONG).show());

        // Get all views...
        ProgressBar progress1 = findViewById(R.id.progressBar);
        ProgressBar progress2 = findViewById(R.id.waitingForConnection);
        TextView text1 = findViewById(R.id.progressText);
        TextView text2 = findViewById(R.id.waitingText);

        // Switch views...
        runOnUiThread(() -> progress1.setVisibility(View.GONE));
        runOnUiThread(() -> progress2.setVisibility(View.VISIBLE));
        runOnUiThread(() -> text1.setVisibility(View.GONE));
        runOnUiThread(() -> text2.setVisibility(View.VISIBLE));

        new Thread(() -> {
            while (true){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo[] netInfo = cm.getAllNetworkInfo();
                boolean connected = false;
                for (NetworkInfo ni : netInfo) {
                    if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                        if (ni.isConnected())
                            if (visible) connected = true;
                    if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                        if (ni.isConnected())
                            if (visible) connected = true;
                }

                if (connected) break;
            }


            // Switch views...
            runOnUiThread(() -> progress1.setVisibility(View.VISIBLE));
            runOnUiThread(() -> progress2.setVisibility(View.GONE));
            runOnUiThread(() -> text1.setVisibility(View.VISIBLE));
            runOnUiThread(() -> text2.setVisibility(View.GONE));

            startLoading();
        }).start();

    }

    private void loadAll() {
        LessonUtils.setWeekdays(new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.weekdays))));

        new Thread(() -> {
            SubjectSymbolsHolder.load(this);
            Log.d("VsaApp/LoadingActivity", "SubjectSymbolsHolder loaded");
            // Load all holders...
            SumsHolder.load(this, () -> checkChangedSums(SumsHolder.getChangedSums()));
        }).start();
    }

    private void checkChangedSums(Map<String, Boolean> sums) {
        if (!SumsHolder.isLoaded()) {noConnection(); return;}
        updateStatus();
        this.sums = sums;

        /*
            Load structure:
            - load sp
            - load vp
            - load dates
            - load ags
            - load documents
            - load teachers
            - finishedLoading
                - start jobs
         */

        // Start with the sp...
        loadSp(sums.get("sp"));
    }

    private void loadSp(boolean update){
        SpHolder.load(this, update, () -> {
            if (!SpHolder.isLoaded()) {noConnection(); return;}
            Log.d("VsaApp/LoadingActivity", "SpHolder loaded");
            updateStatus();
            loadVp(sums.get("vp/today") || sums.get("vp/tomorrow"));
        });
    }

    private void loadVp(boolean update){
        VpHolder.load(this, update, () -> {
            if (!VpHolder.isLoaded()) {noConnection(); return;}
            if (this.getIntent().getStringExtra("day") != null) {
                VpFragment.selectDay(LoadingActivity.this.getIntent().getStringExtra("day"));
            }
            try {
                pushChoices(LoadingActivity.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("VsaApp/LoadingActivity", "VpHolder loaded");
            updateStatus();
            loadDates(sums.get("dates"));
        });
    }

    private void loadDates(boolean update){
        DatesHolder.load(this, update, () -> {
            if (!DatesHolder.isLoaded()) {noConnection(); return;}
            Log.d("VsaApp/LoadingActivity", "DatesHolder loaded");
            updateStatus();
            loadAGs(sums.get("ags"));
        });
    }

    private void loadAGs(boolean update){
        AGsHolder.load(this, update, () -> {
            if (!AGsHolder.isLoaded()) {noConnection(); return;}
            Log.d("VsaApp/LoadingActivity", "AGsHolder loaded");
            updateStatus();
            loadDocs(sums.get("documents"));
        });
    }

    private void loadDocs(boolean update){
        DocumentsHolder.load(this, update, () -> {
            if (!DocumentsHolder.isLoaded()) {noConnection(); return;}
            Log.d("VsaApp/LoadingActivity", "DocumentsHolder loaded");
            updateStatus();
            loadTeachers(sums.get("teachers"));
        });

    }

    private void loadTeachers(boolean update){
        TeacherHolder.load(this, update, () -> {
            if (!TeacherHolder.isLoaded()) {noConnection(); return;}
            Log.d("VsaApp/LoadingActivity", "TeacherHolder loaded");
            updateStatus();
            finishedLoading();
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateStatus(){
        Log.d("VsaApp/LoadingActivity", "Check finish: " + Integer.toString(loaded + 1));
        loaded++;

        TextView progressText = findViewById(R.id.progressText);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        runOnUiThread(() -> {
            progressText.setText(Integer.toString((int) Math.round(((double) loaded / (double) holders) * 100)) + " %");
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", (int) Math.round(((double) loaded / (double) holders) * 100) - 1 / holders, (int) Math.round(((double) loaded / (double) holders) * 100));
            progressAnimator.setDuration(1000);
            progressAnimator.setInterpolator(new LinearInterpolator());
            progressAnimator.start();
        });
    }

    @SuppressLint("SetTextI18n")
    private void finishedLoading() {
        // Control if everything is loaded...


        createJob(this);
        Intent intent = new Intent(this, MainActivity.class);
        if (getIntent().getStringExtra("page") != null) {
            intent.putExtra("page", getIntent().getStringExtra("page"));
        }
        startActivity(intent);
        finish();
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
            Callbacks.credentialsCallback callback = new Callbacks.credentialsCallback() {
                @Override
                public void onSuccess() {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoadingActivity.this);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("pref_username", username.getText().toString());
                    editor.putString("pref_password", password.getText().toString());
                    editor.putString("pref_grade", grades[w[0]]);
                    FirebaseHandler.subscribe(LoadingActivity.this, grades[w[0]]);
                    editor.apply();
                    loginDialog.cancel();
                    Toast.makeText(LoadingActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    updateStatus();
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

}
