package io.github.fmilitao.shopper.sql.queries;

import android.database.Cursor;

import io.github.fmilitao.shopper.sql.DBContract.ItemEntry;

public class SelectDistinctCategories {
    public static final String QUERY = "SELECT DISTINCT " +
            ItemEntry.COLUMN_ITEM_CATEGORY +
            " FROM  " + ItemEntry.TABLE_NAME +
            " WHERE " + ItemEntry.COLUMN_ITEM_CATEGORY + " IS NOT NULL " +
            " ORDER BY " + ItemEntry.COLUMN_ITEM_CATEGORY + " ;";

    private static final int INDEX_NAME = 0;

    public static String getName(Cursor c) {
        return c.getString(INDEX_NAME);
    }
}
