package com.lsb.listProjectBackend.utils;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.github.houbb.opencc4j.util.ZhHkConverterUtil;
import com.github.houbb.opencc4j.util.ZhJpConverterUtil;
import com.github.houbb.opencc4j.util.ZhTwConverterUtil;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ValuePipeline 靜態工具類。
 * <p>
 * 提供 pipeline 的套用、過濾與結果正規化等功能，
 * 不依賴 Spring 容器，可直接以靜態方式呼叫。
 * </p>
 */
public class ValuePipelineUtils {

    private ValuePipelineUtils() {
    }

    /**
     * 從 pipeline 清單中，取出所有已啟用且類型為 {@code USE_REPLACE_VALUE_MAP} 的對應表名稱（不重複）。
     *
     * @param pipelines pipeline 清單
     * @return 不重複的對應表名稱清單；若無符合項目則回傳空清單
     */
    public static List<String> getReplaceValueNameList(List<ValuePipeline> pipelines) {
        if (pipelines == null || pipelines.isEmpty()) {
            return List.of();
        }
        return pipelines.stream()
                .filter(Objects::nonNull)
                .filter(x -> x.isEnabled() && Global.ValuePipelineType.USE_REPLACE_VALUE_MAP.equals(x.getType()))
                .map(ValuePipeline::getUseReplaceValueMap)
                .filter(Utils::isNotBlank)
                .distinct()
                .toList();
    }

    /**
     * 判斷 pipeline 清單中是否存在任何已啟用的項目。
     *
     * @param pipelines pipeline 清單
     * @return 有已啟用的 pipeline 時回傳 {@code true}
     */
    public static boolean hasEnabledPipelines(List<ValuePipeline> pipelines) {
        return !enabledPipelines(pipelines).isEmpty();
    }

    /**
     * 依序套用所有已啟用的 pipeline（依 {@code seq} 升冪排序），
     * 每個 pipeline 的輸出作為下一個 pipeline 的輸入。
     *
     * @param pipelines     pipeline 清單
     * @param pipelineValue 初始值
     * @param context       執行期上下文（result、elements、replaceValueMapList 等）
     * @return 經所有 pipeline 處理後的最終值
     */
    public static Object applyPipelines(List<ValuePipeline> pipelines, Object pipelineValue,
            ValuePipelineContext context) {
        for (final var pipeLine : enabledPipelines(pipelines)) {
            pipelineValue = applyPipeline(pipeLine, pipelineValue, context);
        }
        return pipelineValue;
    }

    /**
     * 套用單一 pipeline。
     * <p>
     * 從 {@code context} 解構出 result、elements、replaceValueMapList，
     * 再委派給 {@link #applyPipelineInternal} 處理。
     * </p>
     *
     * @param pipeLine      要套用的 pipeline
     * @param pipelineValue 目前的值
     * @param context       執行期上下文
     * @return 處理後的值
     */
    public static Object applyPipeline(ValuePipeline pipeLine, Object pipelineValue, ValuePipelineContext context) {
        if (pipeLine == null || pipeLine.getType() == null) {
            return pipelineValue;
        }

        final DocumentContext result = context == null ? null : context.getResult();
        final Elements elements = context == null ? null : context.getElements();
        final List<ReplaceValueMapTO> allReplaceValueMapList = context == null
                ? List.of()
                : context.getReplaceValueMapList();

        return applyPipelineInternal(result, pipeLine, pipelineValue, allReplaceValueMapList, elements);
    }

    /**
     * 對萃取結果進行正規化處理：
     * <ul>
     * <li>List：過濾 null、trim 字串、移除空白字串、去重複</li>
     * <li>String：trim</li>
     * <li>其他：原值回傳</li>
     * </ul>
     *
     * @param pipelineValue 待正規化的值
     * @return 正規化後的值
     */
    public static Object normalizeExtractedValue(Object pipelineValue) {
        if (pipelineValue instanceof List) {
            return ((List<?>) pipelineValue).stream().filter(Objects::nonNull)
                    .map(item -> (item instanceof String) ? ((String) item).trim() : item).filter(item -> {
                        if (item instanceof String) {
                            return Utils.isNotBlank((String) item);
                        }
                        return true;
                    }).distinct().toList();
        }
        if (pipelineValue instanceof String) {
            return ((String) pipelineValue).trim();
        }
        return pipelineValue;
    }

    /** 過濾出已啟用的 pipeline，並依 {@code seq} 升冪排序。 */
    private static List<ValuePipeline> enabledPipelines(List<ValuePipeline> pipelines) {
        if (pipelines == null || pipelines.isEmpty()) {
            return List.of();
        }
        return pipelines.stream()
                .filter(Objects::nonNull)
                .filter(ValuePipeline::isEnabled)
                .sorted(Comparator.comparingInt(ValuePipeline::getSeq))
                .toList();
    }

    /** 依 pipeline 的類型分派處理邏輯，為 {@link #applyPipeline} 的核心實作。 */
    private static Object applyPipelineInternal(DocumentContext result, ValuePipeline pipeLine, Object pipelineValue,
            List<ReplaceValueMapTO> allReplaceValueMapList,
            Elements elements) {
        switch (pipeLine.getType()) {
            // 強制以固定字串取代當前值
            case FIXED_VALUE:
                pipelineValue = Utils.isBlank(pipeLine.getFixedValue()) ? "" : pipeLine.getFixedValue();
                break;
            // 解析 JSON 字串為物件並取代當前值（解析失敗時保留原始字串）
            case FIXED_JSON_VALUE:
                var json = pipeLine.getFixedJsonValue();
                if (Utils.isNotBlank(json)) {
                    try {
                        pipelineValue = JsonPath.parse(json).json();
                    } catch (Exception e) {
                        pipelineValue = json;
                    }
                }
                break;
            // 從 result（DocumentContext）中讀取指定 key 的值
            case CURRENT_DATA_KEY:
                var currentDataKey = pipeLine.getCurrentDataKey();
                if (Utils.isNotBlank(currentDataKey)) {
                    pipelineValue = safeRead(result, currentDataKey);
                }
                break;
            // 從 CSS 選取出的 elements 萃取指定 HTML 屬性值清單
            case EXTRACT_ATTR:
                var attributeName = pipeLine.getAttributeName();
                if (elements != null && !elements.isEmpty() && Utils.isNotBlank(attributeName)) {
                    pipelineValue = elements.stream().map(x -> x.attr(attributeName)).filter(Utils::isNotBlank)
                            .map(String::trim).distinct().toList();
                }
                break;
            // 從 elements 萃取直接文字內容（不含子節點文字）清單
            case EXTRACT_OWN_TEXT:
                if (elements != null && !elements.isEmpty()) {
                    pipelineValue = elements.stream().map(Element::ownText).filter(Utils::isNotBlank).map(String::trim)
                            .distinct().toList();
                }
                break;
            // 以模板字串將 List 組合為單一字串
            case COMBINE_TO_STRING:
                var combineToString = pipeLine.getCombineToString();
                if (pipelineValue instanceof List && Utils.isNotBlank(combineToString)) {
                    pipelineValue = JsonUtils.replaceValueByJsonPath(combineToString, pipelineValue);
                }
                break;
            // 以 JsonPath 模板從 result 合併出字串
            case COMBINE_BY_KEY:
                var combineByKey = pipeLine.getCombineByKey();
                if (Utils.isNotBlank(combineByKey)) {
                    pipelineValue = JsonUtils.replaceValueByJsonPath(combineByKey,
                            result == null ? null : result.json());
                }
                break;
            // 將當前值包裝為單元素 List
            case CONVERT_TO_ARRAY:
                pipelineValue = List.of(pipelineValue);
                break;
            // 取清單第一個元素（若值為 null 則改取 elements 第一個節點文字）
            case FIRST_VALUE:
                if (pipelineValue == null && elements != null && !elements.isEmpty()) {
                    Element firstElement = elements.first();
                    pipelineValue = firstElement == null ? null : firstElement.text();
                }
                if (pipelineValue instanceof List<?> list) {
                    pipelineValue = list.isEmpty() ? "" : list.getFirst();
                }
                break;
            // 取清單最後一個元素（若值為 null 則改取 elements 最後一個節點文字）
            case LAST_VALUE:
                if (pipelineValue == null && elements != null && !elements.isEmpty()) {
                    Element lastElement = elements.last();
                    pipelineValue = lastElement == null ? null : lastElement.text();
                }
                if (pipelineValue instanceof List<?> list) {
                    pipelineValue = list.isEmpty() ? "" : list.getLast();
                }
                break;
            // 以正規表達式取代字串中的匹配內容
            case REPLACE_REGULAR:
                var pattern = pipeLine.getPattern();
                var replacement = Utils.isBlank(pipeLine.getReplacement()) ? "" : pipeLine.getReplacement();
                if (Utils.isNotBlank(pattern)) {
                    if (pipelineValue == null && elements != null && !elements.isEmpty()) {
                        pipelineValue = normalizeExtractedValue(elements.stream().map(Element::text).toList());
                    }
                    if (pipelineValue instanceof List<?> list) {
                        pipelineValue = list.stream()
                                .map(x -> {
                                    if (x instanceof String) {
                                        return ((String) x).replaceAll(pattern, replacement);
                                    }
                                    return x;
                                })
                                .toList();
                    } else {
                        if (pipelineValue instanceof String) {
                            pipelineValue = String.valueOf(pipelineValue).replaceAll(pattern, replacement);
                        }
                    }
                }
                break;
            // 依分隔符號拆分字串為清單
            case SPLIT_TEXT:
                var separator = pipeLine.getSeparator();
                if (Utils.isNotBlank(separator)) {
                    if (pipelineValue instanceof List<?> list) {
                        pipelineValue = list.stream()
                                .flatMap(x -> Arrays.stream(String.valueOf(x).split(separator))).map(String::trim)
                                .filter(Utils::isNotBlank).toList();
                    } else {
                        pipelineValue = Arrays.stream(String.valueOf(pipelineValue).split(separator))
                                .map(String::trim).filter(Utils::isNotBlank).toList();
                    }
                }
                break;
            // 將全部為字串的 List 以指定分隔符號合併為單一字串
            case JOIN_ARRAY_TO_STRING:
                if (pipelineValue instanceof List<?> list) {
                    String joinSeparator = Objects.requireNonNullElse(pipeLine.getJoinSeparator(), "");
                    boolean allStrings = list.stream().allMatch(x -> x instanceof String);
                    if (allStrings) {
                        pipelineValue = list.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(joinSeparator));
                    }
                }
                break;
            // 依對應表（依名稱匹配）將值做轉換
            case USE_REPLACE_VALUE_MAP:
                var replaceValueMapName = pipeLine.getUseReplaceValueMap();
                if (Utils.isNotBlank(replaceValueMapName)) {
                    ReplaceValueMapTO map = allReplaceValueMapList == null ? null
                            : allReplaceValueMapList.stream()
                                    .filter(x -> replaceValueMapName.equals(x.name())).findFirst().orElse(null);
                    if (map != null) {
                        if (pipelineValue instanceof List<?> list) {
                            pipelineValue = list.stream().map(x -> {
                                String xStr = String.valueOf(x);
                                return map.map().getOrDefault(xStr, xStr);
                            }).toList();
                        } else {
                            String xStr = String.valueOf(pipelineValue);
                            pipelineValue = map.map().getOrDefault(xStr, xStr);
                        }
                    }
                }
                break;
            // 從 result 讀取多個 key 對應的物件並合併為陣列
            case MERGE_MULTI_OBJ_TO_ARRAY:
                var mergeMultiObjKeys = pipeLine.getMergeMultiObjKeys();
                if (mergeMultiObjKeys != null && !mergeMultiObjKeys.isEmpty()) {
                    var arr = JsonPath.parse("[]");
                    mergeMultiObjKeys.stream()
                            .map(JsonUtils::ensurePrefix)
                            .map(key -> safeRead(result, key))
                            .filter(Objects::nonNull)
                            .forEach(value -> arr.add("$", value));
                    pipelineValue = arr.json();
                }
                break;
            // 從 result 讀取多個 key 對應的陣列並展平合併為單一陣列
            case MERGE_MULTI_ARRAY_TO_ARRAY:
                var mergeMultiArrayKeys = pipeLine.getMergeMultiArrayKeys();
                if (mergeMultiArrayKeys != null && !mergeMultiArrayKeys.isEmpty()) {
                    var arr = JsonPath.parse("[]");
                    mergeMultiArrayKeys.stream()
                            .map(JsonUtils::ensurePrefix)
                            .map(key -> safeRead(result, key))
                            .forEach(value -> {
                                if (value instanceof List<?>) {
                                    ((List<?>) value).forEach(item -> arr.add("$", item));
                                }
                            });
                    pipelineValue = arr.json();
                }
                break;
            // 字母大小寫轉換（UPPER / LOWER / FIRST_UPPER / FIND_FIRST_UPPER）
            case CONVERT_TO_CASE:
                var convertToCaseType = pipeLine.getConvertToCaseType();
                if (pipelineValue instanceof List<?> list) {
                    pipelineValue = list.stream().map(x -> converterCase(x, convertToCaseType))
                            .toList();
                } else {
                    pipelineValue = converterCase(pipelineValue, convertToCaseType);
                }
                break;
            // 取得當前時間字串（依指定格式與時區）
            case CURRENT_TIME:
                var option = pipeLine.getCurrentTimeFormatOption();
                if (option != null && Utils.isNotBlank(option.getFormat()) && option.getTimezones() != null) {
                    pipelineValue = DateUtils.getCurrentTimeString(option.getFormat(), option.getTimezones());
                } else {
                    pipelineValue = "";
                }
                break;
            // 時間格式轉換（支援時區轉換或略過時區）
            case TIME_FORMAT:
                var timeFormat = pipeLine.getTimeFormat();
                if (timeFormat != null && Utils.isNotBlank(timeFormat.getFormat())
                        && timeFormat.getTimezones() != null) {
                    var format = timeFormat.getFormat();
                    var timezones = timeFormat.getTimezones();
                    var formatParsed = timeFormat.getFormatParsed();
                    var skipTimezoneConversion = timeFormat.isSkipTimezoneConversion();
                    if (pipelineValue instanceof List<?> list) {
                        pipelineValue = list.stream()
                                .map(x -> skipTimezoneConversion
                                        ? DateUtils.convertTimeFormat(String.valueOf(x), formatParsed, format)
                                        : DateUtils.convertTimeFormatWithTimeZone(String.valueOf(x), formatParsed,
                                                format, timezones))
                                .toList();
                    } else {
                        pipelineValue = skipTimezoneConversion
                                ? DateUtils.convertTimeFormat(String.valueOf(pipelineValue), formatParsed, format)
                                : DateUtils.convertTimeFormatWithTimeZone(String.valueOf(pipelineValue),
                                        formatParsed, format, timezones);
                    }
                }
                break;
            // 中文繁簡轉換（支援多種轉換工具）
            case CHINESE_CONVERT:
                var util = pipeLine.getChineseConvert().getZhConverterUtilType();
                var convert = pipeLine.getChineseConvert().getChineseConvertType();
                var toTraditional = convert.equals(Global.ChineseConvertType.SIMPLIFIED_TO_TRADITIONAL);
                if (pipelineValue instanceof List<?> list) {
                    pipelineValue = list.stream()
                            .map(x -> convertChinese(x, toTraditional, util))
                            .toList();
                } else {
                    pipelineValue = convertChinese(pipelineValue, toTraditional, util);
                }
                break;
            // 在字串指定位置（START / END / NTH）插入文字
            case INSERT:
                var insertConfig = pipeLine.getInsertConfig();
                if (insertConfig != null && Utils.isNotBlank(insertConfig.getText())) {
                    var text = insertConfig.getText();
                    var index = insertConfig.getIndex() - 1;
                    var position = insertConfig.getPosition();
                    if (pipelineValue instanceof List<?> list) {
                        pipelineValue = list.stream()
                                .map(item -> item instanceof String s ? applyInsertion(s, text, index, position) : item)
                                .toList();
                    } else if (pipelineValue instanceof String s) {
                        pipelineValue = applyInsertion(s, text, index, position);
                    }
                }
                break;
            // 刪除字串指定位置的若幹字元
            case DELETE:
                var deleteConfig = pipeLine.getDeleteConfig();
                if (deleteConfig != null) {
                    var index = deleteConfig.getIndex() - 1;
                    var length = deleteConfig.getLength();
                    var position = deleteConfig.getPosition();
                    if (pipelineValue instanceof List<?> list) {
                        pipelineValue = list.stream()
                                .map(item -> item instanceof String s ? applyDeletion(s, index, length, position)
                                        : item)
                                .toList();
                    } else if (pipelineValue instanceof String s) {
                        pipelineValue = applyDeletion(s, index, length, position);
                    }
                }
                break;
            // 從 result 中刪除指定 JsonPath 的欄位（直接操作 DocumentContext，不更新 pipelineValue）
            case DELETE_PATHS:
                var deletePaths = pipeLine.getDeletePaths();
                if (deletePaths != null && !deletePaths.isEmpty()) {
                    JsonUtils.deleteValueByPaths(result, deletePaths);
                    return pipelineValue;
                }
                break;
            // 將字串中指定位置的字元移動到另一個位置
            case MOVE_CHAR:
                var moveCharConfig = pipeLine.getMoveCharConfig();
                if (moveCharConfig != null) {
                    var fromIndex = moveCharConfig.getFromIndex() - 1;
                    var toIndex = moveCharConfig.getToIndex();
                    if (pipelineValue instanceof List<?> list) {
                        pipelineValue = list.stream()
                                .map(item -> item instanceof String s
                                        ? Utils.moveChar(s, fromIndex, toIndex)
                                        : item)
                                .toList();
                    } else if (pipelineValue instanceof String s) {
                        pipelineValue = Utils.moveChar(s, fromIndex, toIndex);
                    }
                }
                break;
            // 依算式與變數路徑（從 result 讀取）進行數値計算
            case CALCULATE:
                var calculateConfig = pipeLine.getCalculateConfig();
                if (calculateConfig != null && Utils.isNotBlank(calculateConfig.getExpression())) {
                    var expression = calculateConfig.getExpression();
                    var defaultValue = calculateConfig.getDefaultValue();
                    Map<String, Object> variables = calculateConfig.getVariablePaths().stream()
                            .filter(x -> Utils.isNotBlank(x.getKey()) || Utils.isNotBlank(x.getValue()))
                            .collect(
                                    HashMap::new,
                                    (map, x) -> map.put(x.getKey(), toNumericValue(safeRead(result, x.getValue()))),
                                    HashMap::putAll);
                    try {
                        pipelineValue = ExperessionUtils.calculateExpression(expression, variables).toString();
                    } catch (Exception e) {
                        pipelineValue = safeReadOrReturnKey(result, defaultValue);
                    }
                }
                break;
        }
        return pipelineValue;
    }

    /** 嘗試將字串解析為 {@link BigDecimal}；解析失敗時回傳原字串。 */
    private static Object parseBigDecimalOrSelf(String str) {
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            return str;
        }
    }

    /**
     * 將值轉換為可供算式計算的數値型態。
     * <ul>
     * <li>String → 嘗試解析為 BigDecimal</li>
     * <li>Number → 轉為 BigDecimal</li>
     * <li>List → 各元素嘗試解析，過濾非 BigDecimal</li>
     * <li>其他 → 原值回傳</li>
     * </ul>
     */
    private static Object toNumericValue(Object value) {
        if (value instanceof String str) {
            return parseBigDecimalOrSelf(str);
        }
        if (value instanceof Number) {
            return new BigDecimal(((Number) value).toString());
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> item instanceof String s ? parseBigDecimalOrSelf(s) : item)
                    .filter(v -> v instanceof BigDecimal)
                    .toList();
        }
        return value;
    }

    /**
     * 安全讀取 {@code result} 中指定 JsonPath {@code key} 的值。
     * 路徑不存在時回傳 {@code null}，不拋出例外。
     */
    private static Object safeRead(DocumentContext result, String key) {
        if (result == null || Utils.isBlank(key)) {
            return null;
        }
        try {
            return result.read(key);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     * 安全讀取 {@code result} 中指定 JsonPath {@code key} 的值。
     * 路徑不存在時回傳 {@code key} 本身（供預設値使用）。
     */
    private static Object safeReadOrReturnKey(DocumentContext result, String key) {
        if (result == null || Utils.isBlank(key)) {
            return null;
        }
        try {
            return result.read(key);
        } catch (PathNotFoundException e) {
            return key;
        }
    }

    /** 依 {@code convertToCaseType} 轉換字串的字母大小寫。 */
    private static Object converterCase(Object value, Global.ConvertToCaseType convertToCaseType) {
        if (value instanceof String str && convertToCaseType != null) {
            value = switch (convertToCaseType) {
                case UPPER -> str.toUpperCase();
                case LOWER -> str.toLowerCase();
                case FIRST_UPPER -> Utils.capitalizeFirstLetter(str);
                case FIND_FIRST_UPPER -> Utils.capitalizeFirstLetterByFirstUpper(str);
            };
        }
        return value;
    }

    /** 依 {@code util} 工具類與 {@code toTraditional} 方向進行中文繁簡轉換。 */
    private static Object convertChinese(Object value, boolean toTraditional, Global.ZhConverterUtilType util) {
        if (value instanceof String str) {
            value = switch (util) {
                case ZH_CONVERTER_UTIL -> toTraditional
                        ? ZhConverterUtil.toTraditional(str)
                        : ZhConverterUtil.toSimple(str);
                case ZH_TW_CONVERTER_UTIL -> toTraditional
                        ? ZhTwConverterUtil.toTraditional(str)
                        : ZhTwConverterUtil.toSimple(str);
                case ZH_HK_CONVERTER_UTIL -> toTraditional
                        ? ZhHkConverterUtil.toTraditional(str)
                        : ZhHkConverterUtil.toSimple(str);
                case ZH_JP_CONVERTER_UTIL -> toTraditional
                        ? ZhJpConverterUtil.toTraditional(str)
                        : ZhJpConverterUtil.toSimple(str);
            };
        }
        return value;
    }

    /**
     * 在字串的指定位置（START / END / NTH）插入文字。
     *
     * @param index 目標索引（0-based，呼叫端傳入前已減 1）
     */
    private static String applyInsertion(String value, String insertText, int index, Global.PositionType position) {
        return switch (position) {
            case START -> insertText + value;
            case END -> value + insertText;
            case NTH -> (index >= 0 && index <= value.length())
                    ? value.substring(0, index) + insertText + value.substring(index)
                    : value;
        };
    }

    /**
     * 刪除字串中指定位置（START / END / NTH）的若幹字元。
     *
     * @param index  起始索引（0-based，呼叫端傳入前已減 1）
     * @param length 要刪除的字元數
     */
    private static String applyDeletion(String value, int index, int length, Global.PositionType position) {
        if (value == null || value.isEmpty())
            return value;

        int valueLength = value.length();
        int maxLength = Math.max(0, length);

        return switch (position) {
            case START -> value.substring(Math.min(valueLength, maxLength));
            case END -> value.substring(0, Math.max(0, valueLength - maxLength));
            case NTH -> {
                if (index < 0 || index >= valueLength)
                    yield value;

                int end = Math.min(valueLength, index + maxLength);
                yield value.substring(0, index) + value.substring(end);
            }
        };
    }
}
