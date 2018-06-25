package de.lohl1kohl.vsaapp;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Date {

    private int min = 0;
    private int hour = 0;
    private int day;
    private int month = -1;
    private int year;
    private String weekday;
    private String monthName;
    private List<String> grades = new ArrayList<String>();

    public Date(int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Date(String weekday, int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
        this.weekday = weekday;
    }

    public Date(int day, String monthName, int year){
        this.day = day;
        this.monthName = monthName;
        this.year = year;
    }

    public Date(String weekday, int day, String monthName, int year){
        this.day = day;
        this.monthName = monthName;
        this.year = year;
        this.weekday = weekday;
    }

    public int getTimestamp(Context c){
        return  (year * 100000000) + (getMonth(c) * 1000000) + (day * 10000) + (hour * 100) + min;
    }

    public int getHour(){
        return hour;
    }

    public int getMin(){
        return min;
    }

    public int getDay(){
        return day;
    }

    public int getMonth(Context c){
        if (month == -1) return getMonth(c, monthName);
        else return month;
    }

    public int getYear(){
        return year;
    }

    public String getMonthName(Context c) {
        if (monthName == null) return getMonthName(c, month);
        else return monthName;
    }

    public String getWeekday(Context c) {
        if (weekday == null) return getWeekday(c, day, month, year);
        else return weekday;
    }

    public List<String> getGrades(Context context) {
        if (grades.size() > 0) return grades;
        return new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.nameOfGrades)));
    }

    public void setTime(int min, int hour){
        this.min = min;
        this.hour = hour;
    }

    public void addGrade(String grade){
        this.grades.add(grade);
    }


    private int getMonth(Context context, String monthName){
        return Arrays.asList(context.getResources().getStringArray(R.array.monthNames)).indexOf(monthName) + 1;
    }

    private String getWeekday(Context context, int day, int month, int year){
        java.util.Date now = new java.util.Date(day, month, year);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", Locale.GERMANY);
        String asWeek = dateFormat.format(now);
        Log.i("VsaApp/Date", String.format("%d.%d.%d: %s", day, month, year, asWeek));
        return asWeek;
    }

    private String getMonthName(Context context, int month){
        return Arrays.asList(context.getResources().getStringArray(R.array.monthNames)).get(month - 1);
    }
}
