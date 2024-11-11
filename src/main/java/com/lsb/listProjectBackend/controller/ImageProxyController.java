package com.lsb.listProjectBackend.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@CrossOrigin("*")
@RestController
public class ImageProxyController {
    @GetMapping("/proxy-image")
    public ResponseEntity<byte[]> getImage(@RequestParam String url) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/jpeg");
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                    .body(imageBytes);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
