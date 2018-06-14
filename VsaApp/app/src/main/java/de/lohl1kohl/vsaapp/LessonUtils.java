package de.lohl1kohl.vsaapp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LessonUtils {

    private static int[] startTimes = new int[]{0, 70, 150, 220, 300, 360, 420, 485};
    private static int[] endTimes = new int[]{60, 130, 210, 280, 360, 420, 480, 545};
    private static List<String> weekdays;

    public static boolean isDayInFuture(String day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        return weekdays.indexOf(day) <= weekday;
    }

    public static boolean isLessonPassed(int unit) {
        return getEndTime(unit) < getTimePassed();
    }

    public static void setWeekdays(List<String> days) {
        weekdays = days;
    }

    private static int getStartTime(int unit) {
        return startTimes[unit];
    }

    private static int getEndTime(int unit) {
        return endTimes[unit];
    }

    private static long getTimePassed() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        Date startDate = cal.getTime();
        long diff = new Date().getTime() - startDate.getTime();
        return diff / 1000 / 60;
    }
}
