package de.lohl1kohl.vsaapp;

public class StringUtils {
    public static String poop(String str) {
        return str.replace("ä", "ae").replace("ü", "ue").replace("ö", "oe").replace("Ä", "AE").replace("Ü", "UE").replace("Ö", "OE");
    }
}
