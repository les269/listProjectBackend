package com.lsb.listProjectBackend.utils;

import com.sun.jna.platform.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class Utils {

    /** 判斷字串是否為 null 或空白（含只有空白字元的情況）。 */
    public static boolean isBlank(String str) {
        return null == str || str.trim().isEmpty();
    }

    /**
     * 判斷多個字串中是否有任一為 null 或空白。
     * <p>
     * 只要其中一個為空白即回傳 {@code true}。
     * </p>
     */
    public static boolean isBlank(String... str) {
        if (str == null)
            return true;
        for (String s : str) {
            if (isBlank(s))
                return true;
        }
        return false;
    }

    /** 判斷字串是否不為 null 且不為空白。 */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /** 判斷集合是否為 null 或不含任何元素。 */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }


    /**
     * 將字串中的 {@code ${0}}、{@code ${1}} 等索引佔位符替換為 List 對應位置的值。
     * <p>
     * 若索引不存在則保留原始佔位符。
     * </p>
     *
     * @param value 含 {@code ${index}} 佔位符的模板字串
     * @param arr   替換來源清單
     * @return 替換後的字串
     */
    public static String replaceValue(String value, List<String> arr) {
        Map<String, Object> replaceMap = IntStream.range(0, arr.size()).boxed()
                .collect(Collectors.toMap(i -> i + "", arr::get));
        return replaceValue(value, replaceMap);
    }

    /**
     * 將字串中的 {@code ${key}} 或 {@code ${key.subKey[0]}} 佔位符替換為 Map 對應路徑的值。
     * <p>
     * 若路徑不存在或解析失敗則保留原始佔位符。
     * </p>
     *
     * @param value 含 {@code ${...}} 佔位符的模板字串
     * @param obj   作為資料來源的 Map
     * @return 替換後的字串
     */
    public static String replaceValue(String value, Map<String, Object> obj) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
        Matcher matcher = pattern.matcher(value);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object replacement = resolveKeyPath(key, obj);
            String replacementStr = (replacement != null) ? replacement.toString() : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacementStr));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 依照 {@code key.subKey[index]} 格式逐層解析 Map／List 路徑。
     *
     * @param keyPath 點分隔或陣列標記的路徑字串
     * @param obj     根 Map
     * @return 路徑對應的值；路徑不存在時回傳 {@code null}
     */
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
                return null;
            }
            if (current == null)
                return null;
        }
        return current;
    }

    /** 判斷字串是否可解析為整數。 */
    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 取得 {@link File} 不含副檔名的名稱；若為目錄則直接回傳目錄名稱。
     *
     * @param file 目標檔案或目錄
     * @return 不含副檔名的名稱字串
     */
    public static String getFileNameWithoutExtension(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return (lastDotIndex == -1) ? name : name.substring(0, lastDotIndex);
    }

    /**
     * 檢查檔案是否符合指定的副檔名清單（大小寫不敏感）。
     *
     * @param file      目標檔案
     * @param extension 以逗號分隔的副檔名字串，例如 {@code "jpg,png,gif"}
     * @return 符合其中一個副檔名時回傳 {@code true}
     */
    public static boolean checkFileExtension(File file, String extension) {
        if (isBlank(extension)) {
            return false;
        }
        List<String> extList = Arrays.stream(extension.split(","))
                .map(x -> x.trim().toLowerCase())
                .filter(Utils::isNotBlank)
                .toList();
        if (file.isFile()) {
            String fileName = file.getName().toLowerCase();
            return extList.stream().anyMatch(ext -> fileName.endsWith("." + ext));
        }
        return false;
    }

    /**
     * 將檔案移至資源回收筒以刪除。
     * <p>
     * SMB 網路路徑（{@code \\} 開頭）會略過，不進行刪除。
     * </p>
     *
     * @param path 要刪除的檔案絕對路徑
     * @throws IOException 移動失敗時拋出
     */
    public static void deleteFile(String path) throws IOException {
        File file = new File(path);
        if (file.exists() && !path.startsWith("\\\\")) {
            FileUtils.getInstance().moveToTrash(file);
        }
    }

    /**
     * 將多行文字解析為二維 List。
     * <p>
     * 處理規則：
     * <ul>
     * <li>以換行符（{@code \r\n} 或 {@code \n}）分割為多行</li>
     * <li>每行再以 {@code split} 分隔符切割，並去除前後空白</li>
     * <li>空行或全空白的 token 自動略過</li>
     * </ul>
     *
     * @param text  原始多行文字
     * @param split 每行的欄位分隔符（傳入正規表示式字串）
     * @return 解析後的二維字串 List
     */
    public static List<List<String>> textToList(String text, String split) {
        List<List<String>> result = new ArrayList<>();
        if (isBlank(text)) {
            return result;
        }
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            if (isBlank(line))
                continue;
            List<String> lineItems = Arrays.stream(line.split(split))
                    .map(String::trim)
                    .filter(Utils::isNotBlank)
                    .toList();
            if (!lineItems.isEmpty()) {
                result.add(lineItems);
            }
        }
        return result;
    }

    /**
     * 移除 Windows 檔案名稱中的非法字元，並去除結尾的點與多餘空白。
     * <p>
     * 非法字元包含：{@code \ / : * ? " < > |}
     * </p>
     *
     * @param fileName 原始檔案名稱
     * @return 清理後的檔案名稱
     */
    public static String windowsFileNameReplace(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "").replaceAll("\\.+$", "").trim();
    }

    /**
     * 取得應用程式預設資料目錄下指定檔案的完整路徑。
     * <p>
     * 預設目錄為 {@code %APPDATA%\listProjectData}。
     * </p>
     *
     * @param filename 檔案名稱
     * @return 完整絕對路徑字串
     */
    public static String getDefaultFilePath(String filename) {
        String userHome = System.getProperty("user.home");
        String appDataRoaming = userHome + File.separator + "AppData" + File.separator + "Roaming";
        return Paths.get(appDataRoaming, "listProjectData", filename).toString();
    }

    /**
     * 取得應用程式預設資料目錄的 {@link Path}。
     * <p>
     * 路徑為 {@code %APPDATA%\listProjectData}。
     * </p>
     *
     * @return 預設資料目錄路徑
     */
    public static Path getDefaultDirectoryPath() {
        String userHome = System.getProperty("user.home");
        String appDataRoaming = userHome + File.separator + "AppData" + File.separator + "Roaming";
        return Paths.get(appDataRoaming, "listProjectData");
    }

}
