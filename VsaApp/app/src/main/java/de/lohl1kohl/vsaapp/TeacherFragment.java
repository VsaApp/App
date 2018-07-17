package de.lohl1kohl.vsaapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.lohl1kohl.vsaapp.holder.SubjectSymbolsHolder;
import de.lohl1kohl.vsaapp.holder.TeacherHolder;

import java.util.ArrayList;
import java.util.List;


public class TeacherFragment extends BaseFragment {
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout list;
    private List<Thread> threads = new ArrayList<>();

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void listTeachers(List<Teacher> teachers) {
        List<View> views = new ArrayList<>();
        for (Teacher teacher : teachers) {
            View v = LayoutInflater.from(mActivity).inflate(R.layout.teacher_item, null);
            ((TextView) v.findViewById(R.id.teacherLongName)).setText(teacher.getGenderizedName());
            ((TextView) v.findViewById(R.id.teacherShortName)).setText(teacher.getShortName());
            v.setOnClickListener(view -> {
                final Dialog teacherDialog = new Dialog(mActivity);
                WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
                lWindowParams.copyFrom(teacherDialog.getWindow().getAttributes());
                lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

                teacherDialog.setContentView(R.layout.dialog_teacher_info);
                teacherDialog.setCancelable(true);

                TextView longname = teacherDialog.findViewById(R.id.teacher_dialog_longname);
                TextView shortname = teacherDialog.findViewById(R.id.teacher_dialog_shortname);
                TextView subjects = teacherDialog.findViewById(R.id.teacher_dialog_subjects);
                Button email = teacherDialog.findViewById(R.id.teacher_dialog_email);
                longname.setText(teacher.getGenderizedName());
                shortname.setText(getString(R.string.shortName) + " " + teacher.getShortName());
                StringBuilder s = new StringBuilder().append(getString(R.string.subjects)).append(" ");
                for (String s1 : teacher.getSubjects()) {
                    s.append(SubjectSymbolsHolder.get(s1)).append(", ");
                }
                subjects.setText(s.substring(0, s.length() - 2));
                email.setOnClickListener(view1 -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + teacher.getShortName().toLowerCase() + "@viktoriaschule-aachen.de"));
                    mActivity.startActivity(emailIntent);
                });

                teacherDialog.show();
                teacherDialog.getWindow().setAttributes(lWindowParams);
            });
            views.add(v);
        }
        if (views.size() == 0) {
            views.add(LayoutInflater.from(mActivity).inflate(R.layout.no_teacher, null));
        }
        mActivity.runOnUiThread(() -> {
            list.removeAllViews();
            for (View v : views) {
                list.addView(v);
            }
        });
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

            }

            @Override
            public void afterTextChanged(Editable s) {
                threads.add(new Thread(() -> listTeachers(TeacherHolder.searchTeachers(s.toString()))));
            }
        });
        new Thread(() -> listTeachers(TeacherHolder.getTeachers())).start();
        new Thread(() -> {
            while (true) {
                if (threads.size() != 0) {
                    threads.get(0).start();
                    try {
                        threads.get(0).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    threads.remove(0);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return root;
    }
}