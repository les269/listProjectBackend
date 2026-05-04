package com.lsb.listProjectBackend.service.spider;

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

        return applyPipelineInternal(pipeLine, pipelineValue, allReplaceValueMapList, result, elements);
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

    private Object applyPipelineInternal(ValuePipeline pipeLine, Object pipelineValue,
            List<ReplaceValueMapTO> allReplaceValueMapList, DocumentContext result,
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
                    if (pipelineValue instanceof List) {
                        pipelineValue = ((List<?>) pipelineValue)
                                .stream()
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
                    if (pipelineValue instanceof List) {
                        pipelineValue = ((List<?>) pipelineValue).stream()
                                .flatMap(x -> Arrays.stream(String.valueOf(x).split(separator))).map(String::trim)
                                .filter(Utils::isNotBlank).toList();
                    } else {
                        pipelineValue = Arrays.stream(String.valueOf(pipelineValue).split(separator))
                                .map(String::trim).filter(Utils::isNotBlank).toList();
                    }
                }
                break;
            case JOIN_ARRAY_TO_STRING:
                if (pipelineValue instanceof List) {
                    var anyNullAndNotString = ((List<?>) pipelineValue).stream()
                            .anyMatch(x -> !(x instanceof String));
                    if (!anyNullAndNotString) {
                        var joinSeparator = pipeLine.getJoinSeparator();
                        var list = ((List<?>) pipelineValue).stream().map(String::valueOf).toList();
                        pipelineValue = String.join(joinSeparator == null ? "" : joinSeparator, list);
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
                        if (pipelineValue instanceof List) {
                            pipelineValue = ((List<?>) pipelineValue).stream().map(x -> {
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
                if (pipelineValue instanceof List) {
                    pipelineValue = ((List<?>) pipelineValue).stream().map(x -> converterCase(x, convertToCaseType))
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
                if (pipelineValue instanceof List) {
                    pipelineValue = ((List<?>) pipelineValue).stream()
                            .map(x -> convertChinese(x, toTraditional, util))
                            .toList();
                } else {
                    pipelineValue = convertChinese(pipelineValue, toTraditional, util);
                }
                break;
            case INSERT:
            case COPY_SPECIFIED_VALUE_TO:
            case DELETE:
            case DELETE_PATHS:
            case MOVE_CHAR:
            case JOIN_ARRAY:
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

}
