import org.junit.Before;
import org.junit.Test;
import com.final_project.Youtube;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class YoutubeTest {
    private Youtube youtubeApp;
    private DefaultTableModel tableModel;
    private CountDownLatch latch;

    @Before
    public void setUp() {
        try {
            latch = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    youtubeApp = new Youtube();
                    tableModel = (DefaultTableModel) youtubeApp.table.getModel();
                    latch.countDown();
                }
            });
            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchFunction() {
        try {
            latch = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    tableModel.setRowCount(0);
                    String searchQuery = "coding tutorials";
                    youtubeApp.fetchAndDisplayResults(searchQuery);
                    latch.countDown();
                }
            });
            latch.await(10, TimeUnit.SECONDS);

            int rowCount = tableModel.getRowCount();
            assertTrue(rowCount > 0);

            Object[] firstRow = new Object[tableModel.getColumnCount()];
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                firstRow[i] = tableModel.getValueAt(0, i);
            }
            assertNotNull(firstRow[0]);
            assertNotNull(firstRow[1]);
            assertNotNull(firstRow[2]);
            assertNotNull(firstRow[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSortByTitle() {
        try {
            latch = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    tableModel.setRowCount(0);
                    Object[] row1 = { "Title C", "videoId1", "channelId1", "url1" };
                    Object[] row2 = { "Title A", "videoId2", "channelId2", "url2" };
                    Object[] row3 = { "Title B", "videoId3", "channelId3", "url3" };
                    tableModel.addRow(row1);
                    tableModel.addRow(row2);
                    tableModel.addRow(row3);
                    youtubeApp.sortByTitle();
                    latch.countDown();
                }
            });
            latch.await(5, TimeUnit.SECONDS);

            assertEquals("Title A", tableModel.getValueAt(0, 0));
            assertEquals("Title B", tableModel.getValueAt(1, 0));
            assertEquals("Title C", tableModel.getValueAt(2, 0));

            latch = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    youtubeApp.sortByTitle();
                    latch.countDown();
                }
            });
            latch.await(5, TimeUnit.SECONDS);

            assertEquals("Title C", tableModel.getValueAt(0, 0));
            assertEquals("Title B", tableModel.getValueAt(1, 0));
            assertEquals("Title A", tableModel.getValueAt(2, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}