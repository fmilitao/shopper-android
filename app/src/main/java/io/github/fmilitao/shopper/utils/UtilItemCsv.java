package io.github.fmilitao.shopper.utils;

import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import io.github.fmilitao.shopper.utils.model.Item;

public class UtilItemCsv {

    public static class ClipBoard {

        private static final CsvPreference CSV_PREFERENCE = new CsvPreference
                .Builder('"', ',', "\n")
                .surroundingSpacesNeedQuotes(true)
                .ignoreEmptyLines(true)
                .build();

        private static final String[] HEADERS = {"name", "quantity", "unit"};

        private static final CellProcessor[] CELL_PROCESSORS = new CellProcessor[]{
                // name is not null
                new NotNull(),
                // actually a float but there is only a double parser
                new ParseDouble(),
                // unit is a string that can be null so not processor is needed
                null
        };

        public static List<Item> stringToItemList(String txt) throws IOException {
            return UtilItemCsv.readerToItemList(CSV_PREFERENCE, HEADERS, CELL_PROCESSORS, txt == null ? null : new StringReader(txt), false);
        }

        public static String itemListToString(List<Item> itemList) throws IOException {
            return UtilItemCsv.itemListToString(CSV_PREFERENCE, HEADERS, CELL_PROCESSORS, itemList, false);
        }

    }

    public static class File {

        private static final CsvPreference CSV_PREFERENCE = new CsvPreference
                .Builder('"', ',', "\n")
                .surroundingSpacesNeedQuotes(true)
                .ignoreEmptyLines(true)
                .build();

        private static final String[] HEADERS = {"name", "quantity", "unit", "category", "done"};

        private static final CellProcessor[] W_CELL_PROCESSORS = new CellProcessor[]{
                new NotNull(),
                new ParseDouble(),
                null,
                null,
                new FmtBool("true", "false")
        };

        private static final CellProcessor[] R_CELL_PROCESSORS = new CellProcessor[]{
                new NotNull(),
                new ParseDouble(),
                null,
                null,
                new ParseBool()
        };

        public static List<Item> stringToItemList(String txt) throws IOException {
            return UtilItemCsv.readerToItemList(CSV_PREFERENCE, HEADERS, R_CELL_PROCESSORS, txt == null ? null : new StringReader(txt), false);
        }

        public static List<Item> fileToItemList(java.io.File file) throws IOException {
            return UtilItemCsv.readerToItemList(CSV_PREFERENCE, HEADERS, R_CELL_PROCESSORS, file == null ? null : new FileReader(file), false);
        }

        public static String itemListToString(List<Item> itemList) throws IOException {
            return UtilItemCsv.itemListToString(CSV_PREFERENCE, HEADERS, W_CELL_PROCESSORS, itemList, false);
        }

    }

    private static List<Item> readerToItemList(
            CsvPreference csvPreference,
            String[] headers,
            CellProcessor[] cellProcessors,
            Reader reader,
            boolean includeHeaders
    ) throws IOException {
        final List<Item> result = new ArrayList<>();

        if (reader != null) {
            ICsvBeanReader beanReader = null;
            try {
                beanReader = new CsvBeanReader(reader, csvPreference);

                if (includeHeaders) {
                    beanReader.getHeader(true);
                }

                Item item;
                while ((item = beanReader.read(Item.class, headers, cellProcessors)) != null) {
                    result.add(item);
                }

            } finally {
                if (beanReader != null) {
                    beanReader.close();
                }
            }
        }

        return result;
    }

    private static String itemListToString(
            CsvPreference csvPreference,
            String[] headers,
            CellProcessor[] cellProcessors,
            List<Item> itemList,
            boolean includeHeaders
    ) throws IOException {
        if (itemList == null || itemList.isEmpty()) {
            return "";
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ICsvBeanWriter beanWriter = null;

        try {
            beanWriter = new CsvBeanWriter(new PrintWriter(out), csvPreference);

            if (includeHeaders) {
                beanWriter.writeHeader(headers);
            }

            // write the beans
            for (final Item item : itemList) {
                beanWriter.write(item, headers, cellProcessors);
            }

        } finally {
            if (beanWriter != null) {
                beanWriter.close();
            }
        }

        return new String(out.toByteArray());
    }
}
