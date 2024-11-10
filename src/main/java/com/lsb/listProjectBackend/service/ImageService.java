package com.lsb.listProjectBackend.service;

import java.util.Map;

public interface ImageService {
    void downloadImageFromUrl(String url, String fileDirectoryPath, String fileName, Map<String, String> cookie, String extension, String referer);
}
