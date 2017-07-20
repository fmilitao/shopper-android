package io.github.fmilitao.shopper.sql.queries;

import android.database.Cursor;

import static io.github.fmilitao.shopper.sql.DBContract.ItemEntry;

public class SelectDistinctItemNames {
    public static final String QUERY = "SELECT DISTINCT " +
            ItemEntry.COLUMN_ITEM_NAME +
            " FROM  " + ItemEntry.TABLE_NAME +
            " WHERE " + ItemEntry.COLUMN_DELETED + " = 0 " +
            " ORDER BY " + ItemEntry.COLUMN_ITEM_NAME;

    private static final int INDEX_NAME = 0;

    public static String getName(Cursor c) {
        return c.getString(INDEX_NAME);
    }
}
