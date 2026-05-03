package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionCondition;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Service
public class ExtractionRuleServiceImpl implements ExtractionRuleService {

    @Override
    public List<ExtractionRule> sortedRules(List<ExtractionRule> extractionRules) {
        if (extractionRules == null || extractionRules.isEmpty()) {
            return List.of();
        }
        return extractionRules.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(rule -> rule.getSeq() == null ? Integer.MAX_VALUE : rule.getSeq()))
                .toList();
    }

    @Override
    public List<ValuePipeline> allPipelines(List<ExtractionRule> extractionRules) {
        if (extractionRules == null || extractionRules.isEmpty()) {
            return List.of();
        }
        return extractionRules.stream()
                .filter(Objects::nonNull)
                .map(ExtractionRule::getPipelines)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean checkCondition(ExtractionRule extractionRule, DocumentContext result) {
        if (extractionRule == null || extractionRule.getConditionType() == null) {
            return true;
        }
        ExtractionCondition condition = extractionRule.getConditionValue();
        if (condition == null) {
            return true;
        }

        final var conditionValue = condition.getValue();
        final var value = safeRead(result, condition.getKey());
        final var ignoreCase = condition.isIgnoreCase();

        return switch (extractionRule.getConditionType()) {
            case IF_KEY_EMPTY -> isEffectivelyEmpty(value);
            case IF_KEY_NOT_EMPTY -> !isEffectivelyEmpty(value);
            case CONTAINS -> isContains(value, conditionValue, ignoreCase);
            case NOT_CONTAINS -> !isContains(value, conditionValue, ignoreCase);
            case EQUALS -> isEquals(value, conditionValue, ignoreCase);
            case NOT_EQUALS -> !isEquals(value, conditionValue, ignoreCase);
            case MATCHES -> matchAny(value, s -> Pattern.matches(conditionValue, s));
            case NOT_MATCHES -> !matchAny(value, s -> Pattern.matches(conditionValue, s));
            default -> true;
        };
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

    private boolean isContains(Object value, String conditionValue, boolean ignoreCase) {
        if (conditionValue == null) {
            return false;
        }
        if (value instanceof List) {
            if (ignoreCase) {
                return ((List<?>) value).stream().anyMatch(x -> String.valueOf(x).equalsIgnoreCase(conditionValue));
            }
            return ((List<?>) value).contains(conditionValue);
        }
        if (value instanceof String) {
            if (ignoreCase) {
                return String.valueOf(value).toLowerCase().contains(conditionValue.toLowerCase());
            }
            return String.valueOf(value).contains(conditionValue);
        }
        return false;
    }

    private boolean isEquals(Object value, String conditionValue, boolean ignoreCase) {
        if (conditionValue == null) {
            return false;
        }
        if (value instanceof List) {
            if (ignoreCase) {
                return ((List<?>) value).stream().anyMatch(x -> String.valueOf(x).equalsIgnoreCase(conditionValue));
            }
            return JsonPath.parse(value).jsonString().equals(conditionValue);
        }
        if (value instanceof String) {
            if (ignoreCase) {
                return String.valueOf(value).equalsIgnoreCase(conditionValue);
            }
            return String.valueOf(value).equals(conditionValue);
        }
        return false;
    }

    private boolean isEffectivelyEmpty(Object value) {
        if (value == null) {
            return true;
        }
        return value instanceof List ? ((List<?>) value).isEmpty() : Utils.isBlank(String.valueOf(value));
    }

    private boolean matchAny(Object value, Predicate<String> predicate) {
        if (value instanceof List) {
            return ((List<?>) value).stream().anyMatch(x -> predicate.test(String.valueOf(x)));
        }
        if (value instanceof String) {
            return predicate.test(String.valueOf(value));
        }
        return false;
    }
}

