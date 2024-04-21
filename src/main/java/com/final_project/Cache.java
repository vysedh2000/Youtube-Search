package com.final_project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Cache {
    private static final String CACHE_DIR_NAME = "youtube_api_cache";

    public String checkCache(String urlStr) {
        String response = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder responseBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
                response = responseBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void writeCacheToFile(String url, String data) {
        String filePath = getCacheFilePath(url);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        File cacheDir = new File(System.getProperty("user.home"), CACHE_DIR_NAME);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    private String getCacheFilePath(String url) {
        String cacheDirPath = System.getProperty("user.home") + File.separator + CACHE_DIR_NAME;
        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDirPath + File.separator + url.hashCode() + ".txt";
    }
}
