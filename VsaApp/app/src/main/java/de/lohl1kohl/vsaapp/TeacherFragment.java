package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
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

import java.util.List;

import de.lohl1kohl.vsaapp.holder.TeacherHolder;


public class TeacherFragment extends BaseFragment {
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout list;

    public void listTeachers(List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            @SuppressLint("InflateParams") View v = LayoutInflater.from(mActivity).inflate(R.layout.teacher_item, null);
            ((TextView) v.findViewById(R.id.teacherLongName)).setText(teacher.getGenderizedName());
            ((TextView) v.findViewById(R.id.teacherShortName)).setText(teacher.getShortName());
            TextView mail = v.findViewById(R.id.teacherMail);
            mail.setText(Html.fromHtml("<a href=\"\">@</a>"));
            mail.setOnClickListener(v1 -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + teacher.getShortName().toLowerCase() + "@viktoriaschule-aachen.de"));
                mActivity.startActivity(Intent.createChooser(emailIntent, mActivity.getApplicationContext().getString(R.string.send_email)));
            });
            mActivity.runOnUiThread(() -> list.addView(v));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                    listTeachers(TeacherHolder.searchTeacher(search.getText().toString()));
                }).start();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mActivity.runOnUiThread(list::removeAllViews);
        new Thread(() -> listTeachers(TeacherHolder.getTeachers())).start();

        return root;
    }
}