package com.final_project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Cache {
    private String apiKey;

    public Cache(String apiKey) {
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("No API key provided. Exiting...");
            System.exit(1);
        }
    }

    public String checkCache(String query) {
        String urlStr = "https://www.googleapis.com/youtube/v3/search?q=" + query.replace(" ", "_")
                + "&type=video&part=snippet&key=" + apiKey;
        String cacheFilePath = getCacheFilePath(query);
        File cacheFile = new File(cacheFilePath);

        if (cacheFile.exists() && cacheFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }
                System.out.println(responseBuilder.toString());
                return responseBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null; // return null if there's an error reading the cache file
        }

        // If cache file doesn't exist, make a HTTP request and cache the response
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder responseBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    responseBuilder.append(inputLine).append("\n");
                }
                String response = responseBuilder.toString();
                writeCacheToFile(query, response); // cache the response
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeCacheToFile(String query, String data) {
        String filePath = getCacheFilePath(query);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        File cacheDir = new File("src/main/java/com/final_project/cache"); // adjust the path to your cache directory
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

    private String getCacheFilePath(String query) {
        String cacheDirPath = new File("src/main/java/com/final_project/cache").getAbsolutePath();
        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        System.out.println(cacheDirPath + File.separator + query + ".txt");
        return cacheDirPath + File.separator + query + ".txt";
    }
}
