package com.lsb.listProjectBackend.utils;

public class Global {
    public enum ThemeHeaderType {imageList, table}

    public enum ThemeImageType {key, url}

    public enum ThemeLabelType {string, stringSplit, seq, fileSize, dateFormat}

    public enum ThemeDBType {json, group}

    public enum ThemeCustomType {openUrl, openUrlByKey, writeNote, copyValue, copyValueByKey, buttonIconBoolean, buttonIconFill, buttonInputUrl, apiConfig}

    public enum HttpMethodType {get, post, delete, put}

    public enum ScrapyPageType {redirect, scrapyData}

    public enum ConfigDatasetType {file, folder, all}

    public enum DatasetFieldType {string, stringArray, number, date}
}
