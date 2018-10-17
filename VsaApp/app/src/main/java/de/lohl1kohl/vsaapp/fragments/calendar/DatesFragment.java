package de.lohl1kohl.vsaapp.fragments.calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.skydoves.colorpickerpreference.ColorPickerDialog;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.holders.DatesHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatesFragment extends BaseFragment {

    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dates, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(view -> createEvent());
        createPages(root);

        view = root;

        return root;
    }

    private void createEvent() {
        final Dialog dialog = new Dialog(mActivity);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.setContentView(R.layout.dates_create_event);
        dialog.setCancelable(true);
        dialog.setTitle(R.string.dates_new_event_dialog_title);

        // Get all TextViews...
        final EditText title = dialog.findViewById(R.id.event_title);
        final TextView dateStart = dialog.findViewById(R.id.event_date_start);
        final TextView timeStart = dialog.findViewById(R.id.event_time_start);
        final TextView dateEnd = dialog.findViewById(R.id.event_date_end);
        final TextView timeEnd = dialog.findViewById(R.id.event_time_end);
        final Switch wholeDay = dialog.findViewById(R.id.event_whole_day);
        final Spinner category = dialog.findViewById(R.id.event_category);
        final Button btnOk = dialog.findViewById(R.id.event_btn_ok);
        final Button btnCancel = dialog.findViewById(R.id.event_btn_cancel);

        Calendar now = Calendar.getInstance();
        now.setTime(new java.util.Date());
        dateStart.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR)));
        dateEnd.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR)));
        timeStart.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), now.get(Calendar.HOUR_OF_DAY) + 1, 0));
        timeEnd.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), now.get(Calendar.HOUR_OF_DAY) + 2, 0));

        CategoryAdapter spinnerArrayAdapter = new CategoryAdapter(this, mActivity, R.layout.dates_category, DatesHolder.getCategories(), true);
        category.setAdapter(spinnerArrayAdapter);

        dateStart.setOnClickListener(view -> {
            DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, day) -> {
                month++; // Months from 1 to 12
                dateStart.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                String[] date = dateEnd.getText().toString().split("\\.");
                if (Integer.parseInt(date[2]) < year || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) < month) || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) == month && Integer.parseInt(date[0]) < day)) {
                    dateEnd.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                }
            };

            String[] date = dateStart.getText().toString().split("\\.");
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    mActivity, listener, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));
            datePickerDialog.show();
        });

        timeStart.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener listener = (timePicker, hour, min) -> {
                timeStart.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                String[] time = timeEnd.getText().toString().substring(0, 5).split(":");
                if (Integer.parseInt(time[0]) < hour || (Integer.parseInt(time[0]) == hour && Integer.parseInt(time[1]) < min)) {
                    timeEnd.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                }
            };

            String[] time = timeStart.getText().toString().substring(0, 5).split(":");
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    mActivity, listener, Integer.parseInt(time[0]), Integer.parseInt(time[1]), true);
            timePickerDialog.show();
        });

        dateEnd.setOnClickListener(view -> {
            DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, day) -> {
                month++; // Months from 1 to 12
                dateEnd.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                String[] date = dateStart.getText().toString().split("\\.");
                if (Integer.parseInt(date[2]) > year || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) > month) || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) == month && Integer.parseInt(date[0]) > day)) {
                    dateStart.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                }
            };

            String[] date = dateEnd.getText().toString().split("\\.");
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    mActivity, listener, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));
            datePickerDialog.show();
        });

        timeEnd.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener listener = (timePicker, hour, min) -> {
                timeEnd.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                String[] time = timeStart.getText().toString().substring(0, 5).split(":");
                if (Integer.parseInt(time[0]) > hour || (Integer.parseInt(time[0]) == hour && Integer.parseInt(time[1]) > min)) {
                    timeStart.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                }
            };

            String[] time = timeEnd.getText().toString().substring(0, 5).split(":");
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    mActivity, listener, Integer.parseInt(time[0]), Integer.parseInt(time[1]), true);
            timePickerDialog.show();
        });

        wholeDay.setOnClickListener(view -> {
            timeEnd.setEnabled(!wholeDay.isChecked());
            timeStart.setEnabled(!wholeDay.isChecked());
        });

        btnCancel.setOnClickListener(view -> dialog.cancel());

        btnOk.setOnClickListener(view -> {
            if (title.getText().toString().length() == 0)
                Toast.makeText(mActivity, mActivity.getString(R.string.no_title), Toast.LENGTH_SHORT).show();
            else {
                Date start = new Date(Integer.parseInt(dateStart.getText().toString().split("\\.")[0]), Integer.parseInt(dateStart.getText().toString().split("\\.")[1]), Integer.parseInt(dateStart.getText().toString().split("\\.")[2]));
                Date end;
                if (wholeDay.isChecked()) end = start;
                else {
                    end = new Date(Integer.parseInt(dateEnd.getText().toString().split("\\.")[0]), Integer.parseInt(dateEnd.getText().toString().split("\\.")[1]), Integer.parseInt(dateEnd.getText().toString().split("\\.")[2]));
                    start.setTime(Integer.parseInt(timeStart.getText().toString().substring(0, 5).split(":")[1]), Integer.parseInt(timeStart.getText().toString().split(":")[0]));
                    end.setTime(Integer.parseInt(timeEnd.getText().toString().substring(0, 5).split(":")[1]), Integer.parseInt(timeEnd.getText().toString().split(":")[0]));
                }
                Event event = new Event(title.getText().toString(), start, end, DatesHolder.getCategory(category.getSelectedItemPosition()));
                DatesHolder.addEvent(mActivity, event);
                updateCalendar();
                dialog.cancel();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }

    public void editEvent(Event event) {
        editEvent(null, event);
    }

    public void editEvent(Dialog parentDialog, Event event) {
        final Dialog dialog = new Dialog(mActivity);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.setContentView(R.layout.dates_event);
        dialog.setCancelable(true);
        dialog.setTitle(event.name);

        // Get all TextViews...
        final ImageButton del = dialog.findViewById(R.id.event_del);
        final EditText title = dialog.findViewById(R.id.event_edit_title);
        final TextView dateStart = dialog.findViewById(R.id.event_edit_date_start);
        final TextView timeStart = dialog.findViewById(R.id.event_edit_time_start);
        final TextView dateEnd = dialog.findViewById(R.id.event_edit_date_end);
        final TextView timeEnd = dialog.findViewById(R.id.event_edit_time_end);
        final Switch wholeDay = dialog.findViewById(R.id.event_edit_whole_day);
        final Spinner category = dialog.findViewById(R.id.event_edit_category);
        final Button btnOk = dialog.findViewById(R.id.event_ok);
        final Button btnCancel = dialog.findViewById(R.id.event_cancel);
        final ImageButton export = dialog.findViewById(R.id.export_button);

        if (!DatesHolder.isCustomEvent(event)) {
            title.setEnabled(false);
            dateStart.setEnabled(false);
            timeStart.setEnabled(false);
            dateEnd.setEnabled(false);
            timeEnd.setEnabled(false);
            category.setEnabled(false);
            wholeDay.setEnabled(false);
            del.setEnabled(false);
        } else if (event.start == event.end) {
            timeStart.setEnabled(false);
            timeEnd.setEnabled(false);
        }

        title.setText(event.name);
        wholeDay.setChecked(event.start == event.end);
        dateStart.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", event.start.getDay(), event.start.getMonth(mActivity), event.start.getYear()));
        dateEnd.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", event.end.getDay(), event.end.getMonth(mActivity), event.end.getYear()));
        timeStart.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), event.start.getHour(), event.start.getMin()));
        timeEnd.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), event.end.getHour(), event.end.getMin()));

        CategoryAdapter spinnerArrayAdapter = new CategoryAdapter(this, mActivity, R.layout.dates_category, DatesHolder.getCategories(), true);
        category.setAdapter(spinnerArrayAdapter);

        category.setSelection(DatesHolder.getCategories().indexOf(event.category));

        export.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", event.getStartTime(mActivity));
            intent.putExtra("allDay", event.start.getHour() == event.end.getHour());
            intent.putExtra("endTime", event.getEndTime(mActivity));
            intent.putExtra("title", event.name);
            mActivity.startActivity(intent);
        });

        del.setOnClickListener(view -> {
            DatesHolder.delEvent(mActivity, event);
            updateCalendar();
            dialog.cancel();
        });

        dateStart.setOnClickListener(view -> {
            DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, day) -> {
                month++; // Months from 1 to 12
                dateStart.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                String[] date = dateEnd.getText().toString().split("\\.");
                if (Integer.parseInt(date[2]) < year || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) < month) || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) == month && Integer.parseInt(date[0]) < day)) {
                    dateEnd.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                }
            };

            String[] date = dateStart.getText().toString().split("\\.");
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    mActivity, listener, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));
            datePickerDialog.show();
        });

        timeStart.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener listener = (timePicker, hour, min) -> {
                timeStart.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                String[] time = timeEnd.getText().toString().substring(0, 5).split(":");
                if (Integer.parseInt(time[0]) < hour || (Integer.parseInt(time[0]) == hour && Integer.parseInt(time[1]) < min)) {
                    timeEnd.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                }
            };

            String[] time = timeStart.getText().toString().substring(0, 5).split(":");
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    mActivity, listener, Integer.parseInt(time[0]), Integer.parseInt(time[1]), true);
            timePickerDialog.show();
        });

        dateEnd.setOnClickListener(view -> {
            DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, day) -> {
                month++; // Months from 1 to 12
                dateEnd.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                String[] date = dateStart.getText().toString().split("\\.");
                if (Integer.parseInt(date[2]) > year || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) > month) || (Integer.parseInt(date[2]) == year && Integer.parseInt(date[1]) == month && Integer.parseInt(date[0]) > day)) {
                    dateStart.setText(String.format(Locale.GERMAN, "%02d.%02d.%04d", day, month, year));
                }
            };

            String[] date = dateEnd.getText().toString().split("\\.");
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    mActivity, listener, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));
            datePickerDialog.show();
        });

        timeEnd.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener listener = (timePicker, hour, min) -> {
                timeEnd.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                String[] time = timeStart.getText().toString().substring(0, 5).split(":");
                if (Integer.parseInt(time[0]) > hour || (Integer.parseInt(time[0]) == hour && Integer.parseInt(time[1]) > min)) {
                    timeStart.setText(String.format(Locale.GERMAN, mActivity.getString(R.string.event_time), hour, min));
                }
            };

            String[] time = timeEnd.getText().toString().substring(0, 5).split(":");
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    mActivity, listener, Integer.parseInt(time[0]), Integer.parseInt(time[1]), true);
            timePickerDialog.show();
        });

        wholeDay.setOnClickListener(view -> {
            timeEnd.setEnabled(!wholeDay.isChecked());
            timeStart.setEnabled(!wholeDay.isChecked());
        });

        btnCancel.setOnClickListener(view -> dialog.cancel());

        btnOk.setOnClickListener(view -> {
            if (title.getText().toString().length() == 0)
                Toast.makeText(mActivity, mActivity.getString(R.string.no_title), Toast.LENGTH_SHORT).show();
            else {
                Date start = new Date(Integer.parseInt(dateStart.getText().toString().split("\\.")[0]), Integer.parseInt(dateStart.getText().toString().split("\\.")[1]), Integer.parseInt(dateStart.getText().toString().split("\\.")[2]));
                Date end;
                if (wholeDay.isChecked()) end = start;
                else {
                    end = new Date(Integer.parseInt(dateEnd.getText().toString().split("\\.")[0]), Integer.parseInt(dateEnd.getText().toString().split("\\.")[1]), Integer.parseInt(dateEnd.getText().toString().split("\\.")[2]));
                    start.setTime(Integer.parseInt(timeStart.getText().toString().substring(0, 5).split(":")[1]), Integer.parseInt(timeStart.getText().toString().split(":")[0]));
                    end.setTime(Integer.parseInt(timeEnd.getText().toString().substring(0, 5).split(":")[1]), Integer.parseInt(timeEnd.getText().toString().split(":")[0]));
                }
                Event newEvent = new Event(title.getText().toString(), start, end, DatesHolder.getCategory(category.getSelectedItemPosition()));
                if (DatesHolder.updateEvent(mActivity, event, newEvent)) updateCalendar();
                dialog.cancel();
                if (parentDialog != null) parentDialog.cancel();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }

    private void updateCalendar() {
        ViewPager pager = view.findViewById(R.id.dates_viewpager);
        DatesFragmentAdapter adapter = (DatesFragmentAdapter) pager.getAdapter();
        adapter.update(this);
    }

    public void editCategories(CategoryAdapter parent) {
        final Dialog dialog = new Dialog(mActivity);
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.setContentView(R.layout.dates_edit_categories);
        dialog.setCancelable(true);
        dialog.setTitle(R.string.edit_categories);

        // Get all TextViews...
        final ImageButton add = dialog.findViewById(R.id.category_add);
        final Switch isSchool = dialog.findViewById(R.id.category_is_school);
        final ImageButton del = dialog.findViewById(R.id.category_del);
        final Spinner categoriesList = dialog.findViewById(R.id.categories_list);
        final EditText name = dialog.findViewById(R.id.category_new_name);
        final View color = dialog.findViewById(R.id.category_new_color);
        final Button btnOk = dialog.findViewById(R.id.category_btn_ok);
        final LinearLayout ll = dialog.findViewById(R.id.category_change_color);

        List<Category> categories = new ArrayList<>(DatesHolder.getCategories());

        name.setText(DatesHolder.getCategory(0).name);
        color.setBackgroundColor(Color.rgb(DatesHolder.getCategory(0).color.r, DatesHolder.getCategory(0).color.g, DatesHolder.getCategory(0).color.b));

        CategoryAdapter spinnerArrayAdapter = new CategoryAdapter(this, mActivity, R.layout.dates_category, categories, false);
        categoriesList.setAdapter(spinnerArrayAdapter);

        isSchool.setChecked(!categories.get(categoriesList.getSelectedItemPosition()).isSchool);
        name.setEnabled(!(categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.other_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holiday_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holidays_category))));
        del.setEnabled(!(categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.other_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holiday_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holidays_category))));


        categoriesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                name.setText(categories.get(position).name);
                color.setBackgroundColor(Color.rgb(categories.get(position).color.r, categories.get(position).color.g, categories.get(position).color.b));
                name.setEnabled(!(categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.other_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holiday_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holidays_category))));
                del.setEnabled(!(categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.other_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holiday_category)) || categories.get(categoriesList.getSelectedItemPosition()).name.equals(mActivity.getString(R.string.holidays_category))));
                isSchool.setChecked(!categories.get(categoriesList.getSelectedItemPosition()).isSchool);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ll.setOnClickListener(view -> {
            ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(mActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setTitle(mActivity.getString(R.string.pick_color));
            builder.setPositiveButton(mActivity.getString(R.string.ok), colorEnvelope -> {
                color.setBackgroundColor(colorEnvelope.getColor());
                categories.get(categoriesList.getSelectedItemPosition()).color = new de.lohl1kohl.vsaapp.fragments.calendar.Color(colorEnvelope.getColorRGB()[0], colorEnvelope.getColorRGB()[1], colorEnvelope.getColorRGB()[2]);
            });
            builder.setNegativeButton(mActivity.getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        });

        name.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                boolean isNewName = true;
                for (int i = 0; i < categories.size(); i++) {
                    if (text.equals(categories.get(i).name) && i != categoriesList.getSelectedItemPosition())
                        isNewName = false;
                }
                if (isNewName) categories.get(categoriesList.getSelectedItemPosition()).name = text;
                else {
                    Toast.makeText(mActivity, mActivity.getText(R.string.category_exists), Toast.LENGTH_SHORT).show();
                    name.setText(text.substring(0, text.length() - 1));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        add.setOnClickListener(view -> {
            Category category = new Category("Neu", new de.lohl1kohl.vsaapp.fragments.calendar.Color(0, 0, 255), !isSchool.isChecked());
            categories.add(category);
            spinnerArrayAdapter.data = categories;
            spinnerArrayAdapter.notifyDataSetChanged();
            categoriesList.setSelection(categories.size() - 1);
        });

        del.setOnClickListener(view -> {
            Category category = categories.get(categoriesList.getSelectedItemPosition());
            categories.remove(category);
            spinnerArrayAdapter.data = categories;
            spinnerArrayAdapter.notifyDataSetChanged();
            categoriesList.setSelection(categories.size() - 1);
        });

        btnOk.setOnClickListener(view -> {
            DatesHolder.setCategories(mActivity, categories);
            parent.data = DatesHolder.getCategories();
            parent.notifyDataSetChanged();
            updateCalendar();
            dialog.cancel();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }

    private void createPages(View view) {
        ViewPager pager = view.findViewById(R.id.dates_viewpager);
        DatesFragmentAdapter adapter = new DatesFragmentAdapter(this, mActivity, getFragmentManager());
        pager.setAdapter(adapter);

        // Add the tabs...
        TabLayout tabLayout = view.findViewById(R.id.dates_tabs);
        tabLayout.setupWithViewPager(pager);
    }
}
