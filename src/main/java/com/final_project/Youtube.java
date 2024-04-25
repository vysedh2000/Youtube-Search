package com.final_project;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter.SortKey;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
    private JProgressBar loadingIndicator = new JProgressBar();

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

        pack();
        setVisible(true);
    }

    public void fetchAndDisplayResults(String query) {
        // Create a SwingWorker to fetch data in the background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
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

class URLRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setText((String) value);
        label.setForeground(Color.BLUE);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (table.getRowCount() > row && table.getColumnCount() > column) {
            if (table.getCellRect(row, column, true).contains(table.getMousePosition())) {
                label.setText("<html><u>" + value + "</u></html>");
            } else {
                label.setText((String) value);
            }
        }

        return label;
    }
}
