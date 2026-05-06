package com.lsb.listProjectBackend.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateUtils {

    /**
     * 取得目前時間字串，依指定時區與格式輸出。
     * <p>
     * 若 format 為空，回傳預設的 ISO 格式字串；若 format 非法，回傳空字串。
     * </p>
     */
    public static String getCurrentTimeString(String format, Global.Timezones timezones) {
        ZonedDateTime nowZoned = ZonedDateTime.now(timezones.toZoneId());
        if (Utils.isBlank(format)) {
            return nowZoned.toString();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return nowZoned.format(formatter);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 將時間字串轉換格式，並套用指定時區輸出。
     * <p>
     * 任一必要參數為空時原樣回傳 value；解析失敗亦原樣回傳。
     * </p>
     *
     * @param value        原始時間字串
     * @param inputFormat  輸入格式（可為 null，會自動推斷）
     * @param outputFormat 輸出格式
     * @param timezone     輸出時區
     */
    public static String convertTimeFormatWithTimeZone(
            String value,
            String inputFormat,
            String outputFormat,
            Global.Timezones timezone) {

        if (Utils.isBlank(value) || Utils.isBlank(outputFormat) || timezone == null) {
            return value;
        }

        try {
            TemporalAccessor temporal = parseDateTime(value, inputFormat);
            return DateTimeFormatter
                    .ofPattern(outputFormat)
                    .withZone(timezone.toZoneId())
                    .format(temporal);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 將時間字串轉換格式（不轉換時區）。
     * <p>
     * value 或 outputFormat 為空時原樣回傳 value；解析失敗亦原樣回傳。
     * </p>
     *
     * @param value        原始時間字串
     * @param inputFormat  輸入格式（可為 null，會自動推斷）
     * @param outputFormat 輸出格式
     */
    public static String convertTimeFormat(
            String value,
            String inputFormat,
            String outputFormat) {

        if (Utils.isBlank(value) || Utils.isBlank(outputFormat)) {
            return value;
        }

        try {
            TemporalAccessor temporal = parseDateTime(value, inputFormat);
            return DateTimeFormatter
                    .ofPattern(outputFormat)
                    .format(temporal);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 解析時間字串，依序嘗試 pattern → OffsetDateTime → LocalDateTime → LocalDate。
     */
    static OffsetDateTime parseDateTime(String value, String pattern) {

        if (Utils.isNotBlank(pattern)) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
            try {
                return OffsetDateTime.parse(value, fmt);
            } catch (Exception ignored) {
            }
            try {
                return LocalDateTime.parse(value, fmt).atOffset(ZoneOffset.UTC);
            } catch (Exception ignored) {
            }
            try {
                return LocalDate.parse(value, fmt).atStartOfDay().atOffset(ZoneOffset.UTC);
            } catch (Exception ignored) {
            }
        }

        try {
            return OffsetDateTime.parse(value);
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(value).atOffset(ZoneOffset.UTC);
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(value).atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (Exception ignored) {
        }

        throw new IllegalArgumentException("Unsupported datetime: " + value);
    }
}
