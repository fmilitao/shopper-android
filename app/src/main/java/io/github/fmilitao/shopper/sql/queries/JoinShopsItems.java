package io.github.fmilitao.shopper.sql.queries;

import android.database.Cursor;

import io.github.fmilitao.shopper.sql.DBContract.ItemEntry;
import io.github.fmilitao.shopper.sql.DBContract.ShopEntry;

public class JoinShopsItems {
    private static final String _ID = "_id";
    private static final String JOIN_NAME = "join_name";
    private static final String ALL_ITEMS_COUNT = "all_items_count";
    private static final String DONE_ITEMS_COUNT = "done_items_count";
    private static final String NOT_DONE_ITEMS_COUNT = "not_done_items_count";

    public static final String QUERY = "SELECT " +
            // shop_id
            ("L." + ShopEntry._ID) + " AS " + _ID + ", " +
            // shop_name
            ("L." + ShopEntry.COLUMN_SHOP_NAME) + " AS " + JOIN_NAME + ", " +
            // all_items_count
            "COUNT( R." + ItemEntry._ID + " ) AS " + ALL_ITEMS_COUNT + ", " +
            // items_done_sum
            "IFNULL( SUM( R." + ItemEntry.COLUMN_ITEM_DONE + " ), 0 ) AS " + DONE_ITEMS_COUNT + ", " +
            // items_not_done_sum
            "IFNULL( SUM( NOT R." + ItemEntry.COLUMN_ITEM_DONE + " ), 0 ) AS " + NOT_DONE_ITEMS_COUNT +
            " FROM " +
            // shops table where not deleted
            "(SELECT * FROM " + ShopEntry.TABLE_NAME + " WHERE " + ShopEntry.COLUMN_DELETED + " = 0) L" +
            " LEFT JOIN " +
            // items table where not deleted
            "(SELECT * FROM " + ItemEntry.TABLE_NAME + " WHERE " + ItemEntry.COLUMN_DELETED + " = 0) R" +
            " ON " +
            ("L." + ShopEntry._ID) + " = " + ("R." + ItemEntry.COLUMN_ITEM_SHOP_ID_FK) +
            " GROUP BY " + ("L." + ShopEntry._ID) + " ;";

    // indexes, assumed using the same order as the query above
    private static final int INDEX_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_ALL_ITEMS_COUNT = 2;
    private static final int INDEX_DONE_ITEMS_COUNT = 3;
    private static final int INDEX_NOT_DONE_ITEMS_COUNT = 4;

    public static long getId(Cursor c) {
        return c.getLong(INDEX_ID);
    }

    public static String getName(Cursor c) {
        return c.getString(INDEX_NAME);
    }

    public static String getItemCountString(Cursor c) {
        return c.getString(INDEX_ALL_ITEMS_COUNT);
    }

    public static int getDoneItemCount(Cursor c) {
        return c.getInt(INDEX_DONE_ITEMS_COUNT);
    }

    public static String getDoneItemCountString(Cursor c) {
        return c.getString(INDEX_DONE_ITEMS_COUNT);
    }

    public static int getNotDoneItemCount(Cursor c) {
        return c.getInt(INDEX_NOT_DONE_ITEMS_COUNT);
    }

    public static String getNotDoneItemCountString(Cursor c) {
        return c.getString(INDEX_NOT_DONE_ITEMS_COUNT);
    }
}
