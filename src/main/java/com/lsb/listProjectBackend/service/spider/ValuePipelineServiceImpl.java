package com.lsb.listProjectBackend.service.spider;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.github.houbb.opencc4j.util.ZhHkConverterUtil;
import com.github.houbb.opencc4j.util.ZhJpConverterUtil;
import com.github.houbb.opencc4j.util.ZhTwConverterUtil;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.DeleteConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.InsertConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.MoveCharConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.service.common.ReplaceValueMapService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;

import lombok.RequiredArgsConstructor;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValuePipelineServiceImpl implements ValuePipelineService {
    private final ReplaceValueMapService replaceValueMapService;

    @Override
    public List<ReplaceValueMapTO> fetchReplaceValueMaps(List<ValuePipeline> pipelines) {
        if (pipelines == null || pipelines.isEmpty()) {
            return List.of();
        }
        List<String> names = pipelines.stream()
                .filter(Objects::nonNull)
                .filter(x -> x.isEnabled() && Global.ValuePipelineType.USE_REPLACE_VALUE_MAP.equals(x.getType()))
                .map(ValuePipeline::getUseReplaceValueMap)
                .filter(Utils::isNotBlank)
                .distinct()
                .toList();
        return replaceValueMapService.getAllByNameList(names);
    }

    @Override
    public boolean hasEnabledPipelines(List<ValuePipeline> pipelines) {
        return !enabledPipelines(pipelines).isEmpty();
    }

    @Override
    public Object applyPipelines(List<ValuePipeline> pipelines, Object pipelineValue, ValuePipelineContext context) {
        for (final var pipeLine : enabledPipelines(pipelines)) {
            pipelineValue = applyPipeline(pipeLine, pipelineValue, context);
        }
        return pipelineValue;
    }

    @Override
    public Object applyPipeline(ValuePipeline pipeLine, Object pipelineValue, ValuePipelineContext context) {
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

    @Override
    public Object normalizeExtractedValue(Object pipelineValue) {
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

    private List<ValuePipeline> enabledPipelines(List<ValuePipeline> pipelines) {
        if (pipelines == null || pipelines.isEmpty()) {
            return List.of();
        }
        return pipelines.stream()
                .filter(Objects::nonNull)
                .filter(ValuePipeline::isEnabled)
                .sorted(Comparator.comparingInt(ValuePipeline::getSeq))
                .toList();
    }

    private Object applyPipelineInternal(DocumentContext result, ValuePipeline pipeLine, Object pipelineValue,
            List<ReplaceValueMapTO> allReplaceValueMapList,
            Elements elements) {
        switch (pipeLine.getType()) {
            case FIXED_VALUE:
                pipelineValue = Utils.isBlank(pipeLine.getFixedValue()) ? "" : pipeLine.getFixedValue();
                break;
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
            case CURRENT_DATA_KEY:
                var currentDataKey = pipeLine.getCurrentDataKey();
                if (Utils.isNotBlank(currentDataKey)) {
                    pipelineValue = safeRead(result, currentDataKey);
                }
                break;
            case EXTRACT_ATTR:
                var attributeName = pipeLine.getAttributeName();
                if (elements != null && !elements.isEmpty() && Utils.isNotBlank(attributeName)) {
                    pipelineValue = elements.stream().map(x -> x.attr(attributeName)).filter(Utils::isNotBlank)
                            .map(String::trim).distinct().toList();
                }
                break;
            case EXTRACT_OWN_TEXT:
                if (elements != null && !elements.isEmpty()) {
                    pipelineValue = elements.stream().map(Element::ownText).filter(Utils::isNotBlank).map(String::trim)
                            .distinct().toList();
                }
                break;
            case COMBINE_TO_STRING:
                var combineToString = pipeLine.getCombineToString();
                if (pipelineValue instanceof List && Utils.isNotBlank(combineToString)) {
                    pipelineValue = JsonUtils.replaceValueByJsonPath(combineToString, pipelineValue);
                }
                break;
            case COMBINE_BY_KEY:
                var combineByKey = pipeLine.getCombineByKey();
                if (Utils.isNotBlank(combineByKey)) {
                    pipelineValue = JsonUtils.replaceValueByJsonPath(combineByKey,
                            result == null ? null : result.json());
                }
                break;
            case CONVERT_TO_ARRAY:
                pipelineValue = List.of(pipelineValue);
                break;
            case FIRST_VALUE:
                if (pipelineValue == null && elements != null && !elements.isEmpty()) {
                    Element firstElement = elements.first();
                    pipelineValue = firstElement == null ? null : firstElement.text();
                }
                if (pipelineValue instanceof List<?> list) {
                    pipelineValue = list.isEmpty() ? "" : list.getFirst();
                }
                break;
            case LAST_VALUE:
                if (pipelineValue == null && elements != null && !elements.isEmpty()) {
                    Element lastElement = elements.last();
                    pipelineValue = lastElement == null ? null : lastElement.text();
                }
                if (pipelineValue instanceof List<?> list) {
                    pipelineValue = list.isEmpty() ? "" : list.getLast();
                }
                break;
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
            case JOIN_ARRAY_TO_STRING:
                if (pipelineValue instanceof List<?> list) {
                    String joinSeparator = Objects.requireNonNullElse(pipeLine.getJoinSeparator(), "");
                    // 方案 A：只要 List 內全是 String 才執行（保持你原本的邏輯，但更簡潔）
                    boolean allStrings = list.stream().allMatch(x -> x instanceof String);
                    if (allStrings) {
                        pipelineValue = list.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(joinSeparator));
                    }
                }
                break;
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
            case CONVERT_TO_CASE:
                var convertToCaseType = pipeLine.getConvertToCaseType();
                if (pipelineValue instanceof List<?> list) {
                    pipelineValue = list.stream().map(x -> converterCase(x, convertToCaseType))
                            .toList();
                } else {
                    pipelineValue = converterCase(pipelineValue, convertToCaseType);
                }
                break;
            case CURRENT_TIME:
                var option = pipeLine.getCurrentTimeFormatOption();
                if (option != null && Utils.isNotBlank(option.getFormat()) && option.getTimezones() != null) {
                    pipelineValue = Utils.getCurrentTimeString(option.getFormat(), option.getTimezones());
                } else {
                    pipelineValue = "";
                }
                break;
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
            case INSERT:
                var insertConfig = pipeLine.getInsertConfig();
                if (insertConfig != null && Utils.isNotBlank(insertConfig.getText())) {
                    var text = insertConfig.getText();
                    var index = insertConfig.getIndex() - 1;
                    var position = insertConfig.getPosition();
                    if (pipelineValue instanceof List<?> list) {
                        // 使用 Java 16+ 的 Pattern Matching for instanceof
                        pipelineValue = list.stream()
                                .map(item -> item instanceof String s ? applyInsertion(s, text, index, position) : item)
                                .toList();
                    } else if (pipelineValue instanceof String s) {
                        // 單一數值處理
                        pipelineValue = applyInsertion(s, text, index, position);
                    }
                }
                break;
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
            case DELETE_PATHS:
                var deletePaths = pipeLine.getDeletePaths();
                if (deletePaths != null && !deletePaths.isEmpty()) {
                    JsonUtils.deleteValueByPaths(result, deletePaths);
                    return pipelineValue;
                }
                break;
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
        }
        return pipelineValue;
    }

    private Object safeRead(DocumentContext result, String key) {
        if (result == null || Utils.isBlank(key)) {
            return null;
        }
        try {
            return result.read(key);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    private Object converterCase(Object value, Global.ConvertToCaseType convertToCaseType) {
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

    private Object convertChinese(Object value, boolean toTraditional, Global.ZhConverterUtilType util) {
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

    private String applyInsertion(String value, String insertText, int index, Global.PositionType position) {
        return switch (position) {
            case START -> insertText + value;
            case END -> value + insertText;
            case NTH -> (index >= 0 && index <= value.length())
                    ? value.substring(0, index) + insertText + value.substring(index)
                    : value;
        };
    }

    private String applyDeletion(String value, int index, int length, Global.PositionType position) {
        if (value == null || value.isEmpty())
            return value;

        int valueLength = value.length();
        int maxLength = Math.max(0, length); // 確保長度不為負

        return switch (position) {
            case START -> value.substring(Math.min(valueLength, maxLength));
            case END -> value.substring(0, Math.max(0, valueLength - maxLength));
            case NTH -> {
                if (index < 0 || index >= valueLength)
                    yield value;

                // 計算刪除的結束位置，不超過字串總長
                int end = Math.min(valueLength, index + maxLength);
                yield value.substring(0, index) + value.substring(end);
            }
        };
    }

}
