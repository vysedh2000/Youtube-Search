package com.final_project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter.SortKey;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Youtube extends JFrame {
    private static Cache cache = new Cache();
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public Youtube() {
        super("YouTube Data");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton sortButton = new JButton("Sort by Title");
        JButton clearCacheButton = new JButton("Clear Cache");

        JPanel searchPanel = new JPanel();
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(sortButton);

        table = new JTable();
        model = new DefaultTableModel();
        model.addColumn("Title");
        model.addColumn("Video ID");
        model.addColumn("Channel ID");
        model.addColumn("URL");
        table.setModel(model);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearCacheButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText();
                fetchAndDisplayResults(query);
            }
        });

        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sortByTitle();
            }
        });

        clearCacheButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cache.clearCache();
                System.out.println("Cache cleared.");
            }
        });

        pack();
        setVisible(true);
    }

    private void fetchAndDisplayResults(String query) {
        final String url = "https://www.googleapis.com/youtube/v3/search?q=" + query
                + "&part=snippet&key=AIzaSyAcwnH2DaTJMSa27WMfjbp5KrEpwBk7mhs";

        String data = cache.checkCache(url);

        model.setRowCount(0);

        try {
            if (data != null) {
                JSONObject data_obj = new JSONObject(data);
                JSONArray items = data_obj.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject snippet = item.getJSONObject("snippet");
                    JSONObject id = item.getJSONObject("id");

                    String title = snippet.getString("title");
                    String videoId = id.getString("videoId");
                    String channelId = snippet.getString("channelId");
                    String Url = "https://www.youtube.com/watch?v=" + videoId;

                    model.addRow(new Object[] { title, videoId, channelId, Url });
                }

                cache.writeCacheToFile(url, data);
            } else {
                System.out.println("No data retrieved from cache or API for query: " + query);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean ascendingOrder = true;

    private void sortByTitle() {
        List<? extends SortKey> sortKeys = sorter.getSortKeys();
        if (sortKeys.isEmpty()
                || sortKeys.get(0).getSortOrder() != (ascendingOrder ? SortOrder.ASCENDING : SortOrder.DESCENDING)) {
            sorter.setSortKeys(List.of(new SortKey(0, ascendingOrder ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
        } else {
            ascendingOrder = !ascendingOrder;
            sorter.setSortKeys(List.of(new SortKey(0, ascendingOrder ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Youtube();
            }
        });
    }
}

class Cache {
    private static final String CACHE_DIR_NAME = "youtube_api_cache";

    public String checkCache(String url_str) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(url_str);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public void writeCacheToFile(String url, String data) {
        String filePath = getCacheFilePath(url);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(data);
        } catch (IOException e) {
            System.err.println("Error writing to cache file: " + e.getMessage());
        }
    }

    public void clearCache() {
        File cacheDir = new File(System.getProperty("user.home") + File.separator + CACHE_DIR_NAME);
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
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
