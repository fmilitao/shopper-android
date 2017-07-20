package io.github.fmilitao.shopper.sql.queries;

import android.database.Cursor;

import io.github.fmilitao.shopper.sql.DBContract.ItemEntry;

public class SelectDistinctUnits {
    public static final String QUERY = "SELECT DISTINCT " +
            ItemEntry.COLUMN_ITEM_UNIT +
            " FROM  " + ItemEntry.TABLE_NAME +
            " WHERE " + ItemEntry.COLUMN_ITEM_UNIT + " IS NOT NULL " +
            " ORDER BY " + ItemEntry.COLUMN_ITEM_UNIT + " ;";

    private static final int INDEX_NAME = 0;

    public static String getName(Cursor c) {
        return c.getString(INDEX_NAME);
    }
}
