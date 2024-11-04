package com.lsb.listProjectBackend.utils;

import com.sun.jna.platform.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {
    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean notNull(Object o) {
        return !isNull(o);
    }

    public static boolean isBlank(String str) {
        return null == str || str.trim().isEmpty();
    }

    public static boolean isBlank(String ...str) {
        if (str == null) return true; // Return true if the entire array is null
        for (String s : str) {
            if (isBlank(s)) {
                return true; // Return true if any string is null or blank
            }
        }
        return false;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isNotBlank(String ...str) {
        return !isBlank(str);
    }

    public static String replaceBlank(String oldStr) {
        return isNotBlank(oldStr) ? oldStr : "";
    }

    public static String replaceBlank(String oldStr, String newStr) {
        return isNotBlank(oldStr) ? oldStr : newStr;
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

    public static String replaceValue(String value, List<String> arr) {
        Map<String, Object> replaceMap = IntStream.range(0, arr.size()).boxed().collect(Collectors.toMap(
                i -> i + "",
                arr::get
        ));
        return replaceValue(value, replaceMap);
    }

    public static String replaceValue(String value, Map<String, Object> obj) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
        Matcher matcher = pattern.matcher(value);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object replacement = resolveKeyPath(key, obj);
            // 如果找到值就替换，未找到则保留原样
            matcher.appendReplacement(result, replacement != null ? replacement.toString() : matcher.group(0));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private static Object resolveKeyPath(String keyPath, Map<String, Object> obj) {
        String[] keys = keyPath.split("[.\\[\\]]+");
        Object current = obj;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else if (current instanceof List && isNumeric(key)) {
                current = ((List<Object>) current).get(Integer.parseInt(key));
            } else {
                return null; // 如果路径解析失败，则返回 null
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return (lastDotIndex == -1) ? name : name.substring(0, lastDotIndex);
    }

    public static boolean checkFileExtension(File file, String extension) {
        if (isBlank(extension)) {
            return false;
        }
        if (file.isFile()) {
            return file.getName().toLowerCase().endsWith("." + extension.trim().toLowerCase());
        }
        return false;
    }

    public static void deleteFile(String path) throws IOException {
        File file = new File(path);
        //避免刪除smb檔案
        if(file.exists() && !path.startsWith("\\\\")){
            FileUtils.getInstance().moveToTrash(file);
        }
    }

}
