package com.lsb.listProjectBackend.utils;

public class Global {
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

    public static final String LOCAL_SQLITE_FILE_NAME = "local.sqlite";
    public static final String LOCAL_SQL_FILE_NAME = "local.sql";
    public static final String DYNAMIC_SQLITE_FILE_NAME = "dynamic.sqlite";
    public static final String DYNAMIC_SQL_FILE_NAME = "dynamic.sql";
    public static final String CURRENT_DYNAMIC_DB_SETTING_NAME = "current_dynamic_database";
    public static final String DEFAULT_DYNAMIC_DB_KEY = "default";
    public static final String SQLITE_TYPE = "sqlite";
}
