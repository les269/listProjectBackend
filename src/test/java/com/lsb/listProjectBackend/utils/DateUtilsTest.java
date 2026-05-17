package com.lsb.listProjectBackend.utils;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    // ---------- convertTimeFormat ----------

    @Test
    void convertTimeFormat_localDateTime() {
        String result = DateUtils.convertTimeFormat("2024-03-15T10:30:00", null, "yyyy/MM/dd HH:mm");
        assertEquals("2024/03/15 10:30", result);
    }

    @Test
    void convertTimeFormat_withInputFormat_localDateTime() {
        // inputFormat 指定為 LocalDateTime 格式
        String result = DateUtils.convertTimeFormat("15-03-2024 10:30", "dd-MM-yyyy HH:mm", "yyyy/MM/dd");
        assertEquals("2024/03/15", result);
    }

    @Test
    void convertTimeFormat_withInputFormat_localDate() {
        // inputFormat 指定為 LocalDate 格式
        String result = DateUtils.convertTimeFormat("15/03/2024", "dd/MM/yyyy", "yyyy-MM-dd");
        assertEquals("2024-03-15", result);
    }

    @Test
    void convertTimeFormat_withInputFormat_offsetDateTime() {
        // inputFormat 指定為含時區的格式
        String result = DateUtils.convertTimeFormat(
                "2024-03-15T10:30:00+08:00", "yyyy-MM-dd'T'HH:mm:ssXXXXX", "yyyy/MM/dd HH:mm");
        assertEquals("2024/03/15 10:30", result);
    }

    @Test
    void convertTimeFormat_offsetDateTime() {
        String result = DateUtils.convertTimeFormat("2024-03-15T10:30:00+08:00", null, "yyyy-MM-dd");
        assertEquals("2024-03-15", result);
    }

    @Test
    void convertTimeFormat_localDate() {
        String result = DateUtils.convertTimeFormat("2024-03-15", null, "MM/dd/yyyy");
        assertEquals("03/15/2024", result);
    }

    @Test
    void convertTimeFormat_nullValue_returnsNull() {
        assertNull(DateUtils.convertTimeFormat(null, null, "yyyy-MM-dd"));
    }

    @Test
    void convertTimeFormat_emptyValue_returnsEmpty() {
        assertEquals("", DateUtils.convertTimeFormat("", null, "yyyy-MM-dd"));
    }

    @Test
    void convertTimeFormat_emptyOutputFormat_returnsValue() {
        assertEquals("2024-03-15", DateUtils.convertTimeFormat("2024-03-15", null, ""));
    }

    @Test
    void convertTimeFormat_unparseable_returnsValue() {
        assertEquals("not-a-date", DateUtils.convertTimeFormat("not-a-date", null, "yyyy-MM-dd"));
    }

    // ---------- convertTimeFormatWithTimeZone ----------

    @Test
    void convertTimeFormatWithTimeZone_utcToPlus8() {
        String result = DateUtils.convertTimeFormatWithTimeZone(
                "2024-03-15T02:00:00+00:00", null, "yyyy-MM-dd HH:mm", Global.Timezones.GMT_P08);
        assertEquals("2024-03-15 10:00", result);
    }

    @Test
    void convertTimeFormatWithTimeZone_withInputFormat_localDateTime() {
        // 自訂 inputFormat（LocalDateTime），轉換至 UTC+8
        String result = DateUtils.convertTimeFormatWithTimeZone(
                "15-03-2024 02:00", "dd-MM-yyyy HH:mm", "yyyy-MM-dd HH:mm", Global.Timezones.GMT_P08);
        assertEquals("2024-03-15 10:00", result);
    }

    @Test
    void convertTimeFormatWithTimeZone_withInputFormat_localDate() {
        // 自訂 inputFormat（LocalDate），轉換至 UTC+8（日期不變，時間為 00:00）
        String result = DateUtils.convertTimeFormatWithTimeZone(
                "15/03/2024", "dd/MM/yyyy", "yyyy-MM-dd HH:mm", Global.Timezones.GMT_P08);
        assertEquals("2024-03-15 08:00", result);
    }

    @Test
    void convertTimeFormatWithTimeZone_nullTimezone_returnsValue() {
        String result = DateUtils.convertTimeFormatWithTimeZone(
                "2024-03-15T10:00:00+08:00", null, "yyyy-MM-dd", null);
        assertEquals("2024-03-15T10:00:00+08:00", result);
    }

    @Test
    void convertTimeFormatWithTimeZone_emptyValue_returnsEmpty() {
        assertEquals("", DateUtils.convertTimeFormatWithTimeZone(
                "", null, "yyyy-MM-dd", Global.Timezones.GMT_P08));
    }

    @Test
    void convertTimeFormatWithTimeZone_emptyOutputFormat_returnsValue() {
        assertEquals("2024-03-15T10:00:00+08:00", DateUtils.convertTimeFormatWithTimeZone(
                "2024-03-15T10:00:00+08:00", null, "", Global.Timezones.GMT_P08));
    }

    @Test
    void convertTimeFormatWithTimeZone_unparseable_returnsValue() {
        assertEquals("not-a-date", DateUtils.convertTimeFormatWithTimeZone(
                "not-a-date", null, "yyyy-MM-dd", Global.Timezones.GMT_P08));
    }

    // ---------- parseDateTime ----------

    @Test
    void parseDateTime_withPattern_offsetDateTime() {
        // inputFormat 符合 OffsetDateTime
        OffsetDateTime result = DateUtils.parseDateTime("2024-03-15T10:30:00+08:00", "yyyy-MM-dd'T'HH:mm:ssXXXXX");
        assertEquals(2024, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(10, result.getHour());
    }

    @Test
    void parseDateTime_withPattern_localDateTime() {
        // inputFormat 符合 LocalDateTime（無時區）→ 轉為 UTC offset
        OffsetDateTime result = DateUtils.parseDateTime("15-03-2024 10:30", "dd-MM-yyyy HH:mm");
        assertEquals(2024, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    void parseDateTime_withPattern_localDate() {
        // inputFormat 符合 LocalDate → 轉為當天 00:00 UTC offset
        OffsetDateTime result = DateUtils.parseDateTime("15/03/2024", "dd/MM/yyyy");
        assertEquals(2024, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(0, result.getHour());
    }

    @Test
    void parseDateTime_noPattern_offsetDateTime() {
        OffsetDateTime result = DateUtils.parseDateTime("2024-03-15T10:30:00+08:00", null);
        assertEquals(10, result.getHour());
    }

    @Test
    void parseDateTime_noPattern_localDateTime() {
        OffsetDateTime result = DateUtils.parseDateTime("2024-03-15T10:30:00", null);
        assertEquals(2024, result.getYear());
        assertEquals(10, result.getHour());
    }

    @Test
    void parseDateTime_noPattern_localDate() {
        OffsetDateTime result = DateUtils.parseDateTime("2024-03-15", null);
        assertEquals(2024, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test
    void parseDateTime_unsupported_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateUtils.parseDateTime("not-a-date", null));
    }

    @Test
    void parseDateTime_withPattern_wrongPattern_fallsBackToAuto() {
        // pattern 不符，自動推斷 LocalDateTime
        OffsetDateTime result = DateUtils.parseDateTime("2024-03-15T10:30:00", "dd/MM/yyyy");
        assertEquals(2024, result.getYear());
        assertEquals(10, result.getHour());
    }
}
