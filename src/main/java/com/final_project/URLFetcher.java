package com.final_project;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public class URLFetcher {
    public static String fetch(String urlString) {
        URL url;
        String result;
        try {
            url = new URL(urlString);
            result = IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
            return result;
        } catch (MalformedURLException e) {
            System.out.println("Error MalformedURLException");
            e.printStackTrace();
            return null; // Added return statement
        } catch (IOException e) {
            System.out.println("Error IOException");
            e.printStackTrace();
            return null;
        }
    }
}