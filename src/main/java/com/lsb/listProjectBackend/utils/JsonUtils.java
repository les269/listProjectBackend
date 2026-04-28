package com.lsb.listProjectBackend.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class JsonUtils {

    /**
     * 從 DocumentContext 讀取指定路徑的值。
     * <p>
     * 路徑可省略 {@code $} 前綴，例如 {@code "user.name"} 或 {@code "$.user.name"} 均可。
     * </p>
     *
     * @param ctx      JsonPath 的 DocumentContext
     * @param fullPath JsonPath 路徑，支援 dot-notation 與 bracket-notation
     * @return 路徑對應的值
     * @throws IllegalArgumentException 路徑不存在時拋出
     */
    public static Object getValue(DocumentContext ctx, String fullPath) {
        try {
            return ctx.read(ensurePrefix(fullPath));
        } catch (PathNotFoundException e) {
            throw new IllegalArgumentException("Path not found: " + fullPath, e);
        }
    }

    /**
     * 將值寫入 DocumentContext 的指定路徑，若中間節點不存在會自動建立。
     * <p>
     * 規則：
     * <ul>
     * <li>下一個 token 為 key → 自動補 {@code {}}</li>
     * <li>下一個 token 為 index → 自動補 {@code []}</li>
     * <li>陣列索引超出範圍時，自動以 {@code null} 填充至所需長度</li>
     * </ul>
     * 範例：
     * 
     * <pre>
     * JsonUtils.putValue(ctx, "$.config.request.url", "https://example.com");
     * JsonUtils.putValue(ctx, "$.items[0].name", "alpha");
     * JsonUtils.putValue(ctx, "$.matrix[1][2]", 99);
     * </pre>
     *
     * @param ctx      JsonPath 的 DocumentContext
     * @param fullPath 目標路徑，支援 dot-notation 與 bracket-notation
     * @param value    要寫入的值
     * @throws IllegalArgumentException 路徑為空時拋出
     */
    public static void putValue(DocumentContext ctx, String fullPath, Object value) {
        List<PathToken> tokens = tokenize(ensurePrefix(fullPath));
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Path must not be empty: " + fullPath);
        }

        String currentPath = "$";
        for (int i = 0; i < tokens.size() - 1; i++) {
            PathToken current = tokens.get(i);
            PathToken next = tokens.get(i + 1);
            currentPath = ensureAndMove(ctx, currentPath, current, next);
        }

        PathToken last = tokens.get(tokens.size() - 1);
        putValue(ctx, currentPath, last, value);
    }

    /**
     * 將字串中的 {@code {{$.path}}} 佔位符替換為 {@code obj} 對應路徑的值。
     * <p>
     * 規則：
     * <ul>
     * <li>佔位符內容不以 {@code $} 開頭 → 保留原始佔位符不替換</li>
     * <li>路徑不存在或解析例外 → 保留原始佔位符</li>
     * <li>解析結果為 {@code null} → 保留原始佔位符</li>
     * <li>{@code value} 為 null／空白，或 {@code obj} 為 null → 直接回傳 {@code value}</li>
     * </ul>
     * 範例：
     * 
     * <pre>
     * Map root = Map.of("user", Map.of("name", "Tom"));
     * JsonUtils.replaceValueByJsonPath("hello {{$.user.name}}", root); // → "hello Tom"
     * JsonUtils.replaceValueByJsonPath("hello {{name}}", root); // → "hello {{name}}"
     * </pre>
     *
     * @param value 包含 {@code {{...}}} 佔位符的模板字串
     * @param obj   作為資料來源的物件（Map、List 或任何可供 JsonPath 解析的物件）
     * @return 替換後的字串；若 {@code value} 為 null 則回傳 null
     */
    public static String replaceValueByJsonPath(String value, Object obj) {
        if (Utils.isBlank(value) || obj == null) {
            return value;
        }

        Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");
        Matcher matcher = pattern.matcher(value);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String jsonPath = matcher.group(1).trim();
            if (!jsonPath.startsWith("$")) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            try {
                Object replacement = JsonPath.read(obj, jsonPath);
                String replacementStr = replacement != null ? replacement.toString() : matcher.group(0);
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacementStr));
            } catch (Exception e) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String ensureAndMove(DocumentContext ctx, String currentPath, PathToken current, PathToken next) {
        Object childDefault = next.type == TokenType.KEY ? new HashMap<>() : new ArrayList<>();

        if (current.type == TokenType.KEY) {
            String nextPath = currentPath + "['" + current.key + "']";
            if (!exists(ctx, nextPath)) {
                ctx.put(currentPath, current.key, childDefault);
            }
            return nextPath;
        }

        String nextPath = currentPath + "[" + current.index + "]";
        List<Object> list = readList(ctx, currentPath);
        ensureListSize(list, current.index + 1);
        if (list.get(current.index) == null) {
            list.set(current.index, childDefault);
            ctx.set(currentPath, list);
        }
        return nextPath;
    }

    private static void putValue(DocumentContext ctx, String currentPath, PathToken token, Object value) {
        if (token.type == TokenType.KEY) {
            ctx.put(currentPath, token.key, value);
            return;
        }

        List<Object> list = readList(ctx, currentPath);
        ensureListSize(list, token.index + 1);
        list.set(token.index, value);
        ctx.set(currentPath, list);
    }

    private static boolean exists(DocumentContext ctx, String path) {
        try {
            ctx.read(path);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> readList(DocumentContext ctx, String path) {
        Object node;
        try {
            node = ctx.read(path);
        } catch (PathNotFoundException e) {
            throw new IllegalArgumentException("List path not found: " + path, e);
        }

        if (node == null) {
            List<Object> created = new ArrayList<>();
            ctx.set(path, created);
            return created;
        }
        if (!(node instanceof List<?>)) {
            throw new IllegalArgumentException("Path is not an array: " + path);
        }
        return (List<Object>) node;
    }

    private static void ensureListSize(List<Object> list, int size) {
        while (list.size() < size) {
            list.add(null);
        }
    }

    private static List<PathToken> tokenize(String fullPath) {
        String path = normalizePath(fullPath);
        List<PathToken> tokens = new ArrayList<>();
        StringBuilder key = new StringBuilder();
        int i = 0;

        while (i < path.length()) {
            char c = path.charAt(i);
            if (c == '.') {
                if (key.length() > 0) {
                    tokens.add(PathToken.key(key.toString()));
                    key.setLength(0);
                }
                i++;
                continue;
            }

            if (c == '[') {
                if (key.length() > 0) {
                    tokens.add(PathToken.key(key.toString()));
                    key.setLength(0);
                }

                int close = path.indexOf(']', i);
                if (close < 0) {
                    throw new IllegalArgumentException("Invalid path, missing ]: " + fullPath);
                }

                String inside = path.substring(i + 1, close).trim();
                if ((inside.startsWith("'") && inside.endsWith("'"))
                        || (inside.startsWith("\"") && inside.endsWith("\""))) {
                    tokens.add(PathToken.key(inside.substring(1, inside.length() - 1)));
                } else {
                    try {
                        tokens.add(PathToken.index(Integer.parseInt(inside)));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid array index in path: " + fullPath, e);
                    }
                }
                i = close + 1;
                continue;
            }

            key.append(c);
            i++;
        }

        if (key.length() > 0) {
            tokens.add(PathToken.key(key.toString()));
        }
        return tokens;
    }

    /**
     * 補全 JsonPath 前綴：
     * <ul>
     * <li>已有 {@code $} 開頭 → 原樣回傳</li>
     * <li>{@code [} 開頭（bracket-notation）→ 補 {@code $}</li>
     * <li>其他（dot-notation 裸 key）→ 補 {@code $.}</li>
     * </ul>
     *
     * @param fullPath 原始路徑字串
     * @return 補全前綴後的路徑；null 或空白時原樣回傳
     */
    public static String ensurePrefix(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            return fullPath;
        }
        String path = fullPath.trim();
        if (path.startsWith("$")) {
            return path;
        }
        if (path.startsWith("[")) {
            return "$" + path;
        }
        return "$." + path;
    }

    private static String normalizePath(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            return "";
        }
        String path = fullPath.trim();
        if (path.startsWith("$")) {
            path = path.substring(1);
        }
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        return path;
    }

    private enum TokenType {
        KEY,
        INDEX
    }

    private static class PathToken {
        private final TokenType type;
        private final String key;
        private final int index;

        private PathToken(TokenType type, String key, int index) {
            this.type = type;
            this.key = key;
            this.index = index;
        }

        private static PathToken key(String key) {
            return new PathToken(TokenType.KEY, key, -1);
        }

        private static PathToken index(int index) {
            return new PathToken(TokenType.INDEX, null, index);
        }
    }
}
