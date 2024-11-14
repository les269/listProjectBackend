package com.lsb.listProjectBackend.utils;

public class Global {
    public enum ThemeHeaderType {imageList, table}

    public enum ThemeImageType {key, url}

    public enum ThemeLabelType {string, stringSplit, seq, fileSize, stringArray, date}

    public enum ThemeCustomType {openUrl, openUrlByKey, writeNote, copyValue, copyValueByKey, buttonIconBoolean, buttonIconFill, buttonInputUrl, apiConfig, deleteFile, moveTo}

    public enum ThemeTopCustomType {openUrl, writeNote, apiConfig}

    public enum HttpMethodType {get, post, delete, put}

    public enum ScrapyPageType {redirect, scrapyData}

    public enum ConfigDatasetType {file, folder, all}

    public enum DatasetFieldType {path, fileName, fixedString, fileSize}

    public enum GroupDatasetFieldType {string, stringArray, number, date}

    public enum GroupDatasetConfigType {scrapy, api}
}
