package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.lohl1kohl.vsaapp.holder.Callbacks.vpLoadedCallback;
import de.lohl1kohl.vsaapp.holder.VpHolder;


public class VpFragment extends BaseFragment {
    View vpView;
    private Map<String, String> subjectsSymbols = new HashMap<>();

    @SuppressLint("SetTextI18n")
    static void showVpInfoDialog(Context context, Subject subject) {
        final Dialog loginDialog = new Dialog(context);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(Objects.requireNonNull(loginDialog.getWindow()).getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        loginDialog.setContentView(R.layout.dialog_vp_info);
        loginDialog.setCancelable(true);
        loginDialog.setTitle(R.string.vpInfoDialogTitle);

        String tutorNormal = subject.tutor;
        String tutorNow = subject.changes.tutor;

        // Get long teacher name for normal lesson...
        List<String> shortNames = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.short_names)));
        List<String> longNames = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.long_names)));

        if (tutorNormal.length() > 0) {
            if (shortNames.contains(subject.tutor)) {
                tutorNormal = longNames.get(shortNames.indexOf(tutorNormal));
                tutorNormal = tutorNormal.replace(context.getString(R.string.mister), context.getString(R.string.mister_gen));
            }
        }

        if (tutorNow.length() > 0) {
            if (shortNames.contains(subject.tutor)) {
                tutorNow = longNames.get(shortNames.indexOf(tutorNow));
                tutorNow = tutorNow.replace(context.getString(R.string.mister), context.getString(R.string.mister_gen));
            }
        }

        // Get all TextViews...
        final TextView tV_units = loginDialog.findViewById(R.id.lbl_unit);
        final TextView tV_normal = loginDialog.findViewById(R.id.lbl_normal);
        final TextView tV_changed = loginDialog.findViewById(R.id.lbl_changed);


        tV_units.setText(Integer.toString(subject.unit + 1) + context.getString(R.string.dot_unit));
        tV_normal.setText(String.format(context.getString(R.string.with_s_s_in_room_s), tutorNormal, subject.getName(), subject.room));
        tV_normal.setPaintFlags(tV_normal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        String text;
        if (subject.changes.tutor.length() > 0)
            text = String.format(context.getString(R.string.now_with_s_s), tutorNow, subject.changes.name);
        else text = String.format(context.getString(R.string.now_s), subject.changes.getName());
        if (subject.changes.room.length() > 0)
            text += String.format(context.getString(R.string.in_room_s), subject.changes.room);
        tV_changed.setText(text);

        loginDialog.show();
        loginDialog.getWindow().setAttributes(lWindowParams);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vpView = inflater.inflate(R.layout.fragment_vp, container, false);

        // Update vp...
        vpLoadedCallback callback = new vpLoadedCallback() {
            @Override
            public void onFinished() {
                fillVp();
            }

            @Override
            public void onConnectionFailed() {
                Toast.makeText(mActivity, R.string.no_connection, Toast.LENGTH_SHORT).show();
                fillVp();
            }
        };

        VpHolder.load(callback);

        // Create dictionary with all subject symbols...
        String[] subjects = getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");

            subjectsSymbols.put(pair[0], pair[1]);
        }

        return vpView;
    }

    private void fillVp() {
        ViewPager pager = vpView.findViewById(R.id.vp_viewpager);
        VpDayAdapter adapter = new VpDayAdapter(getFragmentManager());
        pager.setAdapter(adapter);

        // Add the tabs...
        TabLayout tabLayout = vpView.findViewById(R.id.vp_tabs);
        tabLayout.setupWithViewPager(pager);

        if (mActivity.getIntent().getStringExtra("day") != null) {
            if (mActivity.getIntent().getStringExtra("day").equals(VpHolder.weekdayTomorrow)) {
                Objects.requireNonNull(tabLayout.getTabAt(1)).select();
            }
        }
    }
}