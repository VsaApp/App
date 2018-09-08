package de.lohl1kohl.vsaapp.fragments.calendar;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.lohl1kohl.vsaapp.R;

public class Date {

    private int min = 0;
    private int hour = 0;
    private int day;
    private int month = -1;
    private int year;
    private String weekday;
    private String monthName;
    private List<String> grades = new ArrayList<String>();

    public Date(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Date(String weekday, int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.weekday = weekday;
    }

    public Date(int day, String monthName, int year) {
        this.day = day;
        this.monthName = monthName;
        this.year = year;
    }

    public Date(String weekday, int day, String monthName, int year) {
        this.day = day;
        this.monthName = monthName;
        this.year = year;
        this.weekday = weekday;
    }

    public int getTimestamp(Context c) {
        return (year * 100000000) + (getMonth(c) * 1000000) + (day * 10000) + (hour * 100) + min;
    }

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }

    public int getDay() {
        return day;
    }

    public int getMonth(Context c) {
        if (month == -1) return getMonth(c, monthName);
        else return month;
    }

    public int getYear() {
        return year;
    }

    public String getMonthName(Context c) {
        if (monthName == null) return getMonthName(c, month);
        else return monthName;
    }

    public String getWeekday(Context c) {
        if (weekday == null) return getWeekdayString(c);
        else return weekday;
    }

    public int getDayOfWeek(Context c) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, getMonth(c) - 1);
        calendar.set(Calendar.YEAR, year);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (weekday == -1) weekday = 6;
        return weekday;
    }

    public List<String> getGrades(Context context) {
        if (grades.size() > 0) return grades;
        return new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.nameOfGrades)));
    }

    public void setTime(int min, int hour) {
        this.min = min;
        this.hour = hour;
    }

    public void addGrade(String grade) {
        this.grades.add(grade);
    }


    private int getMonth(Context context, String monthName) {
        return Arrays.asList(context.getResources().getStringArray(R.array.monthNames)).indexOf(monthName) + 1;
    }

    private String getWeekdayString(Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.monthNames)).get(getDayOfWeek(context));
    }

    private String getMonthName(Context context, int month) {
        return Arrays.asList(context.getResources().getStringArray(R.array.monthNames)).get(month - 1);
    }
}
