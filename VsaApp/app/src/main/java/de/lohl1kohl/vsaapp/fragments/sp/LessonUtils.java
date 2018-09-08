package de.lohl1kohl.vsaapp.fragments.sp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LessonUtils {

    public static int[] endTimes = new int[]{60, 130, 210, 280, 360, 420, 480, 545};
    private static int[] startTimes = new int[]{0, 70, 150, 220, 300, 360, 420, 485};
    private static List<String> weekdays;

    public static boolean isDayPassed(String day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (weekday == -1 | weekday == 5 | isFridayEvening()) weekday = 0;
        return weekdays.indexOf(day) < weekday;
    }

    public static boolean isDayInFuture(String day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (weekday == 5 | isFridayEvening()) weekday = -1;
        return weekdays.indexOf(day) > weekday;
    }

    private static boolean isFridayEvening() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekday == 6) {
            return isSchoolAtDayPassed(4);
        }
        return false;
    }

    private static boolean isSchoolAtDayPassed(int day) {
        int lastLesson = SpHolder.getDay(day).size() - 1;
        return isLessonPassed(lastLesson);
    }

    public static boolean isLessonPassed(int unit) {
        return endTimes[unit] < getTimePassed();
    }

    public static void setWeekdays(List<String> days) {
        weekdays = days;
    }

    private static long getTimePassed() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        long diff = new Date().getTime() - startDate.getTime();
        return diff / (long) 1000 / (long) 60;
    }
}
