package edu.yale.library.paperless.services;

public class StringHelper {

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static String defaultValue(String str, String defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        return str;
    }
}
