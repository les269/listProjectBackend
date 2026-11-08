package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.entity.dynamic.spider.EndTimeConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.ParamSetting;
import com.lsb.listProjectBackend.utils.DateUtils;
import com.lsb.listProjectBackend.utils.Utils;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 參數展開服務使用笛卡爾積方式。
 */
@Service
public class SpiderPaginationParameterService {
    public List<Map<String, Object>> expand(List<ParamSetting> settings, LocalDateTime now) {
        if (settings == null) {
            return List.of();
        }
        settings.sort(Comparator.comparing(ParamSetting::getSeq, Comparator.nullsLast(Integer::compareTo)));
        // 需要先放入一組空Map才可以進行第一次的複製，當沒有Map會導致沒有任何組合產生
        List<Map<String, Object>> combinations = List.of(new LinkedHashMap<>());
        for (ParamSetting setting : settings) {
            List<String> values = getValues(setting, now);// 取得該參數的所有可能值
            // 如果該參數沒有值，則跳過該參數
            if (values == null || values.isEmpty()) {
                continue;
            }
            combinations = combineParameter(combinations, setting.getParamKey(), values);
        }
        // 如果只有一組參數，且該組參數是空的，則表示沒有任何參數需要展開，回傳空清單
        if (combinations.size() == 1 && combinations.get(0).isEmpty()) {
            return List.of();
        }
        return combinations;
    }

    private List<Map<String, Object>> combineParameter(List<Map<String, Object>> combinations,
            String key, List<String> values) {
        List<Map<String, Object>> expanded = new ArrayList<>();
        for (Map<String, Object> current : combinations) {
            for (String value : values) {
                Map<String, Object> next = new LinkedHashMap<>(current);
                next.put(key, value);
                expanded.add(next);
            }
        }
        return expanded;
    }

    private List<String> getValues(ParamSetting setting, LocalDateTime now) {
        return switch (setting.getType()) {
            case STATIC_LIST -> staticValues(setting);
            case NUMBER_RANGE -> numberValues(setting);
            case TIME_RANGE -> timeValues(setting, now);
        };
    }

    private List<String> staticValues(ParamSetting setting) {
        if (setting.getStaticList() == null) {
            return List.of();
        }
        return setting.getStaticList().stream().filter(s -> Utils.isNotBlank(s)).toList();
    }

    private List<String> numberValues(ParamSetting setting) {
        requirePositive(setting.getNumStep(), "numStep");
        BigInteger start = new BigInteger(setting.getStartNumStr());
        BigInteger end = new BigInteger(setting.getEndNumStr());
        BigInteger step = BigInteger.valueOf(setting.getNumStep());
        int width = setting.getStartNumStr().replaceFirst("^[+-]", "").length();
        var isPadZeros = setting.getPadZeros() != null && setting.getPadZeros();

        return Stream.iterate(start, x -> x.compareTo(end) <= 0, x -> x.add(step))
                .map(value -> {
                    String digits = value.abs().toString();
                    if (isPadZeros) {
                        digits = "0".repeat(Math.max(0, width - digits.length())) + digits;
                    }
                    return Utils.isNegative(value) ? "-" + digits : digits;
                }).toList();
    }

    private List<String> timeValues(ParamSetting setting, LocalDateTime now) {
        requirePositive(setting.getTimeInterval(), "timeInterval");
        if (setting.getStartTime() == null || setting.getTimeIntervalUnit() == null
                || setting.getTimeFormat() == null || setting.getTimeFormat().isBlank()) {
            throw new IllegalArgumentException("Time parameters require startTime, timeIntervalUnit and timeFormat");
        }
        LocalDateTime end = calcEndTime(setting.getEndTimeConfig(), now);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(setting.getTimeFormat());
        return Stream.iterate(setting.getStartTime(), value -> !value.isAfter(end),
                value -> DateUtils.addTime(value, setting.getTimeInterval(), setting.getTimeIntervalUnit()))
                .map(formatter::format)
                .toList();
    }

    private LocalDateTime calcEndTime(EndTimeConfig config, LocalDateTime now) {
        if (config == null || config.getEndTimeType() == null) {
            throw new IllegalArgumentException("Time parameters require endTimeConfig.endTimeType");
        }
        return switch (config.getEndTimeType()) {
            case FIXED -> {
                if (config.getEndTime() == null) {
                    throw new IllegalArgumentException("FIXED endTime requires endTime");
                }
                yield config.getEndTime();
            }
            case CURRENT_DATE -> now;
            case END_CURRENT_YEAR -> now.toLocalDate().withDayOfYear(1).plusYears(1).atStartOfDay().minusNanos(1);
            case RELATIVE_OFFSET -> {
                if (config.getOffsetInterval() == null) {
                    throw new IllegalArgumentException("RELATIVE_OFFSET requires offsetInterval");
                }
                yield DateUtils.addTime(now, config.getOffsetInterval(), config.getOffsetUnit());
            }
        };
    }

    private void requirePositive(Integer value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
