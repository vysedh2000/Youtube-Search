package com.final_project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter.SortKey;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Youtube extends JFrame {
    private static Cache cache = new Cache("", 20);
    public JTextField searchField;
    public JButton searchButton;
    public JButton sortButton;
    public JButton clearCacheButton;
    public JTable table;
    public DefaultTableModel model;
    public TableRowSorter<DefaultTableModel> sorter;
    public JProgressBar loadingIndicator;

    public Youtube() {

        super("YouTube Data");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        sortButton = new JButton("Sort by Title");
        clearCacheButton = new JButton("Clear Cache");
        loadingIndicator = new JProgressBar();
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(sortButton);
        loadingIndicator.setVisible(false);
        add(loadingIndicator, BorderLayout.NORTH);

        // Create a loading indicator

        table = new JTable();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (col == 3) { // URL column
                        String url = (String) table.getValueAt(row, col);
                        try {
                            Desktop desktop = Desktop.getDesktop();
                            URI uri = new URI(url);
                            desktop.browse(uri);
                        } catch (IOException | URISyntaxException ex) {
                            System.err.println("Error opening URL: " + ex.getMessage());
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == 3) {
                    table.setToolTipText("Open URL");
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                table.setCursor(Cursor.getDefaultCursor());
            }
        });

        model = new DefaultTableModel();
        model.addColumn("Title");
        model.addColumn("Video ID");
        model.addColumn("Channel ID");
        model.addColumn("URL");
        table.setModel(model);
        // table.getColumnModel().getColumn(3).setCellRenderer(new URLRenderer());

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel mainPanel = new JPanel(
                new BorderLayout());
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
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0); //
                searchField.setText("");
                cache.clearCache();
                System.out.println("Cache cleared.");
            }
        });
        setLocationRelativeTo(null); // Center the JFrame on the screen
        pack();
        setVisible(true);
    }

    public void fetchAndDisplayResults(String query) {
        // Create a SwingWorker to fetch data in the background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                searchButton.setEnabled(false); // Disable the search button
                loadingIndicator.setVisible(true);

                // Fetch data from cache or API
                String data = cache.checkCache(query);
                if (data != null) {
                    // Parse JSON data
                    JSONObject data_obj = new JSONObject(data);
                    JSONArray items = data_obj.getJSONArray("items");

                    // Create a list to store the data
                    List<Object[]> dataList = new ArrayList<>();
                    int itemCount = items.length();
                    int progress = 0;

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        JSONObject snippet = item.getJSONObject("snippet");
                        JSONObject id = item.getJSONObject("id");

                        String title = snippet.getString("title");
                        String videoId = id.getString("videoId");
                        // if (videoId == null || videoId.isEmpty()) {
                        // videoId = id.getString("channelId");
                        // }
                        String channelId = snippet.getString("channelId");
                        String Url = "https://www.youtube.com/watch?v=" + videoId;

                        dataList.add(new Object[] { title, videoId, channelId, Url });
                        progress = (int) (((double) i / itemCount) * 100);
                        searchButton.setEnabled(true); // Disable the search button
                        loadingIndicator.setValue(progress);
                    }

                    // Cache the data
                    cache.writeCacheToFile(query, data);

                    // Update the UI with the fetched data
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            model.setRowCount(0);
                            for (Object[] row : dataList) {
                                model.addRow(row);
                            }
                        }
                    });
                } else {
                    System.out.println("No data retrieved from cache or API for query: " + query);
                }

                return null;
            }

            @Override
            protected void done() {
                // Remove the loading indicator
                loadingIndicator.setVisible(false);
            }
        };

        // Start the SwingWorker
        worker.execute();
    }

    private boolean ascendingOrder = true;

    public void sortByTitle() {
        // Sort the table based on title column
        List<? extends SortKey> sortKeys = sorter.getSortKeys();
        if (sortKeys.isEmpty()
                || sortKeys.get(0).getSortOrder() != (ascendingOrder ? SortOrder.ASCENDING : SortOrder.DESCENDING)) {
            ascendingOrder = true; // Ensure ascending order
        } else {
            ascendingOrder = !ascendingOrder; // Toggle the sorting order
        }
        System.out.println("Sorting order: " + (ascendingOrder ? "Ascending" : "Descending")); // Debugging statement
        sorter.setSortKeys(List.of(new SortKey(0, ascendingOrder ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Youtube();
            }
        });
    }
}
