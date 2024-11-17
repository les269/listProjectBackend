package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    public void downloadImageFromUrl(String url, String fileDirectoryPath, String fileName, Map<String, String> cookie, String extension, String referer) {

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            var uri = new URI(url);
            Connection connection = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .header("Referer", uri.getScheme() + "://" + uri.getHost())
                    .header("Accept", "*/*")
                    .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 4.2.1; Nexus 7 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166  Safari/535.19");
            if (url.startsWith("https://")) {
                connection.sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
            }
            if (cookie != null) {
                connection.cookies(cookie);
            }
            if (referer != null) {
                connection.header("Referer", referer);
            }
            Connection.Response response = connection.execute();
            if (response.statusCode() == 200) {
                byte[] imageData = response.bodyAsBytes();
                String filePath = fileDirectoryPath + File.separator + fileName;
                if (extension != null) {
                    filePath += "." + extension;
                }

                try (OutputStream out = new FileOutputStream(filePath)) {
                    out.write(imageData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
