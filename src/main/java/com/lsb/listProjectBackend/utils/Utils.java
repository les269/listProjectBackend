package com.lsb.listProjectBackend.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static boolean isNull(Object o){return o == null;}

    public static boolean notNull(Object o){return !isNull(o);}
    public static boolean isBlank(String str) {
        return null == str || "".equals(str);
    }

    public static String replaceBlank(String oldStr) {
        return isNotBlank(oldStr) ? oldStr : "";
    }
    public static String replaceBlank(String oldStr, String newStr) {
        return isNotBlank(oldStr) ? oldStr : newStr;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean objIsEmpty(Object object) {
        return object == null;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static String filePathConvert(String path) {
        return path.substring(8);
    }

    public static final Character[] INVALID_WINDOWS_SPECIFIC_CHARS = {'"', '*', ':', '<', '>', '?', '\\', '|', 0x7F};

    public static boolean validateStringFilenameUsingRegex(String filename) {
        if (filename == null || filename.isEmpty() || filename.length() > 255) {
            return false;
        }
        return Arrays.stream(INVALID_WINDOWS_SPECIFIC_CHARS)
                .noneMatch(ch -> filename.contains(ch.toString()));
    }

    public static String getListOne(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return list.get(0);
    }

    public static String joinStringList(List<String>... list) {
        String str = "";
        List<String> s = new ArrayList<>();

        for (List<String> ss : list) {
            if (ss != null) {
                s.addAll(ss);
            }
        }
        s = s.stream().distinct().sorted().collect(Collectors.toList());
        for (String ss : s) {
            str = str + ss + ",";
        }
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static boolean containsCaseInsensitive(String strToCompare, List<String> list) {
        for (String str : list) {
            if (str.equalsIgnoreCase(strToCompare)) {
                return (true);
            }
        }
        return (false);
    }

    public static String matchStr(Pattern pattern, String s) {
        Matcher m = pattern.matcher(s);
        m.find();
        try {
            return m.group();
        } catch (Exception e) {
            return s;
        }
    }

    public static String getFileName(String name) {
        return name.substring(0, name.indexOf("."));
    }

}
