package com.lsb.listProjectBackend.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ImageProxyController {
    @GetMapping("/proxy-image")
    public ResponseEntity<byte[]> getImage(@RequestParam String url) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String scheme = url.split("://")[0] + "://";
            String remainingUrl = url.substring(scheme.length());
            String host = remainingUrl.split("/")[0];
            String path = url.substring((scheme + host + "/").length());
            path = URLEncoder.encode(path, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            byte[] imageBytes = restTemplate.getForObject(URI.create(scheme + host + "/" + path), byte[].class);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                    .body(imageBytes);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
