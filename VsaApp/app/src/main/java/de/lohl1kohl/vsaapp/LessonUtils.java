package de.lohl1kohl.vsaapp;

public class LessonUtils {

    private static int[] startTimes = new int[]{0, 70, 150, 220, 300, 360, 420, 485};
    private static int[] endTimes = new int[]{60, 130, 210, 280, 360, 420, 480, 545};

    public static int getStartTime(Lesson lesson) {
        return startTimes[lesson.subjects.get(0).unit];
    }

    public static int getEndTime(Lesson lesson) {
        return endTimes[lesson.subjects.get(0).unit];
    }
}
