package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class TeacherFragment extends BaseFragment {
    private static String[] shortNames;
    private static String[] longNames;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout list;

    public static void listTeachers(Activity activity, List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            @SuppressLint("InflateParams") View v = LayoutInflater.from(activity).inflate(R.layout.teacher_item, null);
            ((TextView) v.findViewById(R.id.teacherLongName)).setText(teacher.lName);
            ((TextView) v.findViewById(R.id.teacherShortName)).setText(teacher.sName);
            TextView mail = v.findViewById(R.id.teacherMail);
            mail.setText(Html.fromHtml("<a href=\"\">@</a>"));
            mail.setOnClickListener(v1 -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + teacher.sName.toLowerCase() + "@viktoriaschule-aachen.de"));
                activity.startActivity(Intent.createChooser(emailIntent, activity.getApplicationContext().getString(R.string.send_email)));
            });
            activity.runOnUiThread(() -> list.addView(v));
        }
    }

    public static List<Teacher> searchTeacher(String str) {
        List<Teacher> teachers = new ArrayList<>();
        for (int i = 0; i < shortNames.length; i++) {
            String sName = shortNames[i];
            String lName = longNames[i];
            if (sName.toLowerCase().contains(str.toLowerCase())) {
                teachers.add(new Teacher(sName, lName));
            } else if (lName.toLowerCase().substring(5).contains(str.toLowerCase())) {
                teachers.add(new Teacher(sName, lName));
            }
        }
        return teachers;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shortNames = mActivity.getResources().getStringArray(R.array.short_names);
        longNames = mActivity.getResources().getStringArray(R.array.long_names);
        View root = inflater.inflate(R.layout.fragment_teacher, container, false);
        EditText search = root.findViewById(R.id.teacherSearch);
        list = root.findViewById(R.id.teacherList);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new Thread(() -> {
                    mActivity.runOnUiThread(list::removeAllViews);
                    List<Teacher> teachers = searchTeacher(search.getText().toString());
                    listTeachers(mActivity, teachers);
                }).start();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        new Thread(() -> {
            List<Teacher> teachers = searchTeacher("");
            listTeachers(mActivity, teachers);
        }).start();
        return root;
    }

    static class Teacher {
        private String sName;
        private String lName;

        Teacher(String shortName, String longName) {
            sName = shortName;
            lName = longName;
        }
    }
}