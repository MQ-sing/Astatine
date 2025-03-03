package com.sing.astatine.utils;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static String replaceFormatSpecifiers(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char current = input.charAt(i);
            if (current == '%') {
                if (i + 1 < length && input.charAt(i + 1) == '%') {
                    result.append("%%");
                    i++;
                    continue;
                }
                int start = i++;
                StringBuilder position = new StringBuilder();
                boolean hasPosition = false;

                while (i < length && Character.isDigit(input.charAt(i))) {
                    position.append(input.charAt(i++));
                }
                if (i < length && input.charAt(i) == '$') {
                    hasPosition = true;
                    i++;
                } else {
                    i = start + 1;
                }
                while (i < length && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    i++;
                }
                if (i < length && (input.charAt(i) == 'd' || input.charAt(i) == 'f')) {
                    result.append('%');
                    if (hasPosition) {
                        result.append(position).append('$');
                    }
                    result.append('s');
                    i++;
                } else {
                    i = start;
                    result.append(input.charAt(i));
                }
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }
    public static boolean hasClass(String name) {
        try {
            Class.forName(name,false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
    public static boolean startsWithIgnoreCase(String text,String find){
        return text.regionMatches(true,0,find,0,find.length());
    }
    public static boolean anyMatch(String toFind,String... target){
        for (String s : target) {
            if(startsWithIgnoreCase(s,toFind))return true;
        }
        return false;
    }
    public static boolean createFile(File file) throws IOException {
        if(file.exists())return false;
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        return file.createNewFile();
    }
}
