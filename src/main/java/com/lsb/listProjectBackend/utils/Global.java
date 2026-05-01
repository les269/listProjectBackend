package com.lsb.listProjectBackend.utils;

import lombok.Getter;

import java.time.ZoneId;
import java.util.Map;

public class Global {

    public static final String LOCAL_SQLITE_FILE_NAME = "local.sqlite";
    public static final String LOCAL_SQL_FILE_NAME = "local.sql";
    public static final String DYNAMIC_SQLITE_FILE_NAME = "dynamic.sqlite";
    public static final String DYNAMIC_SQL_FILE_NAME = "dynamic.sql";
    public static final String CURRENT_DYNAMIC_DB_SETTING_NAME = "current_dynamic_database";
    public static final String DEFAULT_DYNAMIC_DB_KEY = "default";
    public static final String SQLITE_TYPE = "sqlite";

    public enum ThemeHeaderType {
        imageList, table
    }

    public enum ThemeImageType {
        key, url
    }

    public enum ThemeLabelType {
        string, stringSplit, seq, fileSize, stringArray, date
    }

    public enum ThemeCustomType {
        openUrl, writeNote, copyValue, buttonIconBoolean, buttonIconFill, buttonInputUrl, apiConfig, deleteFile, moveTo,
        openFolder
    }

    public enum ThemeTopCustomType {
        openUrl, writeNote, apiConfig
    }

    public enum HttpMethodType {
        get, post, delete, put
    }

    public enum ScrapyPageType {
        redirect, scrapyData
    }

    public enum DatasetConfigType {
        file, folder, all, text, pagination
    }

    public enum DatasetFieldType {
        path, fileName, fixedString, fileSize
    }

    public enum GroupDatasetFieldType {
        string, stringArray, number, date
    }

    public enum GroupDatasetConfigType {
        scrapy, api
    }

    public enum OpenWindowTargetType {
        _self, _blank, _parent, _top
    }

    public enum UpdateIntervalType {
        year, month, day
    }

    public enum QuickRefreshType {
        params, url
    }

    public enum ThemeItemType {
        IMAGE, LABEL, DATASET, CUSTOM, TAG, OTHERSETTING, TOPCUSTOM
    }

    public enum CookieListMapType {
        SPIDER, API
    }

    public enum SpiderUrlType {
        BY_PARAMS, BY_PRIME_KEY
    }

    public enum ExtractionRuleMode {
        SELECT, JSON_PATH
    }

    public enum ExtractionStepCondition {
        ALWAYS,
        IF_KEY_EMPTY,
        IF_KEY_NOT_EMPTY,
        CONTAINS,
        NOT_CONTAINS,
        EQUALS,
        NOT_EQUALS,
        MATCHES,
        NOT_MATCHES
    }

    public enum ValuePipelineType {
        FIXED_VALUE,
        FIXED_JSON_VALUE,
        EXTRACT_ATTR,
        EXTRACT_OWN_TEXT,
        REPLACE_REGULAR,
        SPLIT_TEXT,
        CONVERT_TO_ARRAY,
        FIRST_VALUE,
        LAST_VALUE,
        COMBINE_TO_STRING,
        COMBINE_BY_KEY,
        USE_REPLACE_VALUE_MAP,
        MERGE_MULTI_OBJ_TO_ARRAY,
        MERGE_MULTI_ARRAY_TO_ARRAY,
        CONVERT_TO_CASE,
        CURRENT_TIME,
        CHINESE_CONVERT,
        INSERT,
        COPY_SPECIFIED_VALUE_TO,
        DELETE,
        DELETE_PATHS,
        MOVE_CHAR,
        JOIN_ARRAY
    }

    public enum PositionType {
        START,
        END,
        NTH
    }

    public enum ConvertToCaseType {
        UPPER,
        LOWER,
        FIRST_UPPER,
        FIND_FIRST_UPPER
    }

    public enum ChineseConvertType {
        SIMPLIFIED_TO_TRADITIONAL,
        TRADITIONAL_TO_SIMPLIFIED
    }


    public enum Timezones {
        GMT_M12("GMT-12:00"),
        GMT_M11("GMT-11:00"),
        GMT_M10("GMT-10:00"),
        GMT_M09("GMT-09:00"),
        GMT_M08("GMT-08:00"),
        GMT_M07("GMT-07:00"),
        GMT_M06("GMT-06:00"),
        GMT_M05("GMT-05:00"),
        GMT_M04("GMT-04:00"),
        GMT_M03("GMT-03:00"),
        GMT_M02("GMT-02:00"),
        GMT_M01("GMT-01:00"),
        GMT_P00("GMT+00:00"),
        GMT_P01("GMT+01:00"),
        GMT_P02("GMT+02:00"),
        GMT_P03("GMT+03:00"),
        GMT_P04("GMT+04:00"),
        GMT_P05("GMT+05:00"),
        GMT_P06("GMT+06:00"),
        GMT_P07("GMT+07:00"),
        GMT_P08("GMT+08:00"),
        GMT_P09("GMT+09:00"),
        GMT_P10("GMT+10:00"),
        GMT_P11("GMT+11:00"),
        GMT_P12("GMT+12:00");

        private final String value;

        Timezones(String value) {
            this.value = value;
        }

        /**
         * 快速轉換為 Java ZoneId
         */
        public ZoneId toZoneId() {
            return ZoneId.of(this.value);
        }
    }
}
