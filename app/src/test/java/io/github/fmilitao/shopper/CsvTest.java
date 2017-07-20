package io.github.fmilitao.shopper;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.github.fmilitao.shopper.utils.UtilItemCsv;
import io.github.fmilitao.shopper.utils.model.Item;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


/**
 * Tests reading and writing Items to CSV strings.
 */
final public class CsvTest {

    /**
     * Tests reading a single item from a string.
     * Each element can have spaces around it so that a name " item " is considered
     * to be just "item".
     */
    @Test
    public void testSingleStringToItemListParsing() throws Exception {
        // must ignore spaces between elements
        final String[] strings = {
                "item,12,unit",
                "item, 12,unit",
                " item , 12 , unit "
        };

        for (final String line : strings) {
            final List<Item> items = UtilItemCsv.ClipBoard.stringToItemList(line);

            assertEquals(1, items.size());

            final Item item = items.get(0);

            assertEquals("item", item.getName());
            assertEquals(12.0, item.getQuantity());
            assertEquals("unit", item.getUnit());

            // not set by the strings
            assertNull(item.getCategory());
            assertEquals(-1, item.getId());
        }
    }

    /**
     * Tests parsing corner cases.
     */
    @Test
    public void testEmptyStringToItemListParsing() throws Exception {
        assertNotNull(UtilItemCsv.ClipBoard.stringToItemList(null));
        assertTrue(UtilItemCsv.ClipBoard.stringToItemList(null).isEmpty());
        assertTrue(UtilItemCsv.ClipBoard.stringToItemList("").isEmpty());
    }

    /**
     * Tests parsing a multiline CSV string.
     */
    @Test
    public void testMultipleStringToItemListParsing() throws Exception {
        final List<Item> items = UtilItemCsv.ClipBoard.stringToItemList(
                " item0 , 123 , unit0 \n \" item1 \" , 543 , unit1 \nitem2,1,"
        );

        assertEquals(3, items.size());


        final Item item0 = items.get(0);

        assertEquals("item0", item0.getName());
        assertEquals(123.0, item0.getQuantity());
        assertEquals("unit0", item0.getUnit());

        // not set by the strings
        assertNull(item0.getCategory());
        assertEquals(-1, item0.getId());


        final Item item1 = items.get(1);

        assertEquals(" item1 ", item1.getName());
        assertEquals(543.0, item1.getQuantity());
        assertEquals("unit1", item1.getUnit());

        // not set by the strings
        assertNull(item1.getCategory());
        assertEquals(-1, item1.getId());


        final Item item2 = items.get(2);

        assertEquals("item2", item2.getName());
        assertEquals(1.0, item2.getQuantity());
        assertNull(item2.getUnit());

        // not set by the strings
        assertNull(item2.getCategory());
        assertEquals(-1, item2.getId());
    }

    /**
     * Tests writing a single item
     */
    @Test
    public void testSingleItemWriting() throws Exception {
        final Item item = new Item(-1, " item with spaces ", "  unit", 234.234234, "not shown", true);

        assertEquals(
                "\" item with spaces \",234.234234,\"  unit\"\n",
                UtilItemCsv.ClipBoard.itemListToString(Collections.singletonList(item))
        );
    }

    @Test
    public void testSingleItemWritingWithHeaders() throws Exception {
        final Item item = new Item(-1, " item with spaces ", "  unit", 234.234234, "cat", true);

        assertEquals(
//                "name,quantity,unit,category,done\n\" item with spaces \",234.234234,\"  unit\",cat,true\n",
                "\" item with spaces \",234.234234,\"  unit\",cat,true\n",
                UtilItemCsv.File.itemListToString(Collections.singletonList(item))
        );
    }

    /**
     * Tests writing multiple items to string.
     */
    @Test
    public void testMultipleItemWriting() throws Exception {
        final List<Item> items = Arrays.asList(
                new Item(-1, "a", "aa", 11.11, "not shown", true),
                new Item(-1, "b", "bb", 123.123, "not shown", true)
        );

        assertEquals(
                "a,11.11,aa\nb,123.123,bb\n",
                UtilItemCsv.ClipBoard.itemListToString(items)
        );
    }

    /**
     * Tests writer corner cases.
     */
    @Test
    public void testEmptyItemWriting() throws Exception {
        assertEquals("", UtilItemCsv.ClipBoard.itemListToString(null));
        assertEquals("", UtilItemCsv.ClipBoard.itemListToString(new LinkedList<Item>()));
    }


    /**
     * Tests round trip of passing an item list to string and then back.
     */
    @Test
    public void testRoundTripClipboard() throws Exception {
        final List<Item> items = Arrays.asList(
                new Item(-1, " www \" a", "aa ok !!", 11.1101, "not shown asd", true),
                new Item(-1, "b why not", "bb", 123.123, "not shown", false)
        );

        final List<Item> result = UtilItemCsv.ClipBoard.stringToItemList(
                UtilItemCsv.ClipBoard.itemListToString(items)
        );

        assertEquals(items.size(), result.size());

        for (int i = 0; i < items.size(); ++i) {
            final Item expected = items.get(i);
            final Item actual = result.get(i);

            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getQuantity(), actual.getQuantity());
            assertEquals(expected.getUnit(), actual.getUnit());

            // not set by the strings
            assertNull(actual.getCategory());
            assertEquals(-1, actual.getId());
        }
    }

    @Test
    public void testRoundTripFile() throws Exception {
        final List<Item> items = Arrays.asList(
                new Item(-1, " www \" a", "aa ok !!", 11.1101, "not shown asd", true),
                new Item(-1, "b why not", "bb", 123.123, "not shown", false)
        );

        final List<Item> result = UtilItemCsv.File.stringToItemList(
                UtilItemCsv.File.itemListToString(items)
        );

        assertEquals(items.size(), result.size());

        for (int i = 0; i < items.size(); ++i) {
            final Item expected = items.get(i);
            final Item actual = result.get(i);

            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getQuantity(), actual.getQuantity());
            assertEquals(expected.getUnit(), actual.getUnit());
            assertEquals(expected.getCategory(), actual.getCategory());
            assertEquals(expected.isDone(), actual.isDone());

            assertEquals(-1, actual.getId());
        }
    }

    @Test
    public void testFileLoad() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("items.csv").getFile());

        assertTrue(file.exists());

        List<Item> items = UtilItemCsv.File.fileToItemList(file);

        assertEquals(2, items.size());

        final Item item0 = items.get(0);

        assertEquals("My item is", item0.getName());
        assertEquals(123.0, item0.getQuantity());
        assertEquals("unit1", item0.getUnit());
        assertEquals("cat1", item0.getCategory());
        assertEquals(true, item0.isDone());

        assertEquals(-1, item0.getId());

        final Item item1 = items.get(1);

        assertEquals("item ok", item1.getName());
        assertEquals(0.0, item1.getQuantity());
        assertEquals(null, item1.getUnit());
        assertEquals(null, item1.getCategory());
        assertEquals(false, item1.isDone());

        assertEquals(-1, item1.getId());
    }
}
