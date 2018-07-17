package de.lohl1kohl.vsaapp.holder;

import android.content.Context;
import de.lohl1kohl.vsaapp.R;

import java.util.HashMap;
import java.util.Map;

public class SubjectSymbolsHolder {

    private static Map<String, String> subjectSymbols = new HashMap<>();

    public static void load(Context context) {
        String[] subjects = context.getResources().getStringArray(R.array.nameOfSubjects);
        for (String subject : subjects) {
            String[] pair = subject.split(":");
            subjectSymbols.put(pair[0], pair[1]);
            if (!pair[0].replaceAll("[0-9]", "").equals(pair[0])) {
                subjectSymbols.put(pair[0].replaceAll("[0-9]", ""), pair[1]);
            }
        }
    }

    public static boolean has(String str) {
        return subjectSymbols.containsKey(str);
    }

    public static String get(String str) {
        return subjectSymbols.get(str);
    }
}
