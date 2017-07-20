package io.github.fmilitao.shopper.sql.queries;

import android.database.Cursor;

import io.github.fmilitao.shopper.sql.DBContract;

public class SelectShopItemsQuantities {
    private static final String ALL_ITEMS_COUNT = "all_items_count";
    private static final String NOT_DONE_ITEMS_COUNT = "not_done_items_count";

    public static final String QUERY = "SELECT " +
            "COUNT( " + DBContract.ItemEntry._ID + " ) AS " + ALL_ITEMS_COUNT + ", " +
            "IFNULL( SUM( NOT " + DBContract.ItemEntry.COLUMN_ITEM_DONE + " ), 0 ) AS " +
            NOT_DONE_ITEMS_COUNT +
            " FROM " + DBContract.ItemEntry.TABLE_NAME + " WHERE " +
            DBContract.ItemEntry.COLUMN_DELETED + " = 0 AND " +
            DBContract.ItemEntry.COLUMN_ITEM_SHOP_ID_FK + "=? ;";

    // indexes of query above, if order above changes so must the values below
    private static final int INDEX_ALL_ITEMS = 0;
    private static final int INDEX_NOT_DONE = 1;

    public static int getItemCount(Cursor c) {
        return c.getInt(INDEX_ALL_ITEMS);
    }

    public static int getNotDoneItemCount(Cursor c) {
        return c.getInt(INDEX_NOT_DONE);
    }
}
