import com.final_project.Youtube;
import org.junit.Before;
import org.junit.Test;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import static org.junit.Assert.*;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;

public class YoutubeTest {

    private Youtube youtube;
    private JTextField searchField;
    private JButton searchButton;
    private JButton sortButton;
    private JButton clearCacheButton;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JProgressBar loadingIndicator;

    @Before
    public void setUp() throws Exception {
        youtube = new Youtube();
        searchField = youtube.searchField;
        searchButton = youtube.searchButton;
        sortButton = youtube.sortButton;;
        clearCacheButton = youtube.clearCacheButton;
        table = youtube.table;
        model = youtube.model;
        sorter = youtube.sorter;
        loadingIndicator = youtube.loadingIndicator;
    }

    @Test
    public void testComponentsExistence() {
        assertNotNull("Search field should not be null", searchField);
        assertNotNull("Search button should not be null", searchButton);
        assertNotNull("Sort button should not be null", sortButton);
        assertNotNull("Clear cache button should not be null", clearCacheButton);
        assertNotNull("Table should not be null", table);
        assertNotNull("Model should not be null", model);
        assertNotNull("Sorter should not be null", sorter);
        assertNotNull("Loading indicator should not be null", loadingIndicator);
    }

    @Test
    public void testFetchAndDisplayResults() {
        String searchQuery = "coding";
        youtube.fetchAndDisplayResults(searchQuery);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int rowCount = table.getRowCount();
        assertTrue(rowCount > 0);

        // Check if the first row contains data
        Object[] firstRow = new Object[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            firstRow[i] = table.getValueAt(0, i);
        }
        assertNotNull(firstRow[0]);
        assertNotNull(firstRow[1]);
        assertNotNull(firstRow[2]);
        assertNotNull(firstRow[3]);
    }

    @Test
    public void testSortByTitle() {
        model.addRow(new Object[]{"Title C", "videoId1", "channelId1", "url1"});
        model.addRow(new Object[]{"Title A", "videoId2", "channelId2", "url2"});
        model.addRow(new Object[]{"Title B", "videoId3", "channelId3", "url3"});

        sorter.setModel(model);
        table.setRowSorter(sorter);

        youtube.sortByTitle(); // Assume this sorts in ascending order first
        assertEquals("Title A should be first after sorting ascending", "Title A", table.getValueAt(0, 0));
        assertEquals("Title B should be second", "Title B", table.getValueAt(1, 0));
        assertEquals("Title C should be third", "Title C", table.getValueAt(2, 0));

        youtube.sortByTitle(); // Assume calling again toggles to descending order
        assertEquals("Title C should be first after sorting descending", "Title C", table.getValueAt(0, 0));
        assertEquals("Title B should be second", "Title B", table.getValueAt(1, 0));
        assertEquals("Title A should be third", "Title A", table.getValueAt(2, 0));
    }
    @Test
    public void testEmptySearchResults() {
        searchField.setText("");
        searchButton.doClick();

        // Expect no change in the table's row count and appropriate UI behavior
        assertEquals("Table should be empty after an empty search", 0, table.getRowCount());
        assertFalse("Loading indicator should not be visible after search", loadingIndicator.isVisible());
    }

    @Test
    public void testSearchFunctionality() {
        String searchQuery = "java";
        searchField.setText(searchQuery);
        searchButton.doClick();

        // Assume the search is performed asynchronously
        try {
            Thread.sleep(3000); // wait for the search to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue("Table should have rows after search", table.getRowCount() > 0);
        assertEquals("Search field should contain the query", searchQuery, searchField.getText());
    }

    @Test
    public void testClearCacheFunctionality() {
        model.addRow(new Object[]{"Video 1", "videoId4", "channelId4", "url4"});
        clearCacheButton.doClick();

        assertEquals("Table should be empty after clearing the cache", 0, table.getRowCount());
    }

}
