package io.github.fmilitao.shopper.sql.queries;

import android.database.Cursor;

import io.github.fmilitao.shopper.sql.DBContract;

public class SelectShops {
    public static final String QUERY = "SELECT " +
            DBContract.ShopEntry._ID + ", " +
            DBContract.ShopEntry.COLUMN_SHOP_NAME +
            " FROM  " + DBContract.ShopEntry.TABLE_NAME +
            " WHERE " + DBContract.ShopEntry.COLUMN_DELETED + " = 0 ;";

    // indexes of query above, if order above changes so must the values below
    private static final int INDEX_ID = 0;
    private static final int INDEX_NAME = 1;

    public static long getId(Cursor c) {
        return c.getLong(INDEX_ID);
    }

    public static String getName(Cursor c) {
        return c.getString(INDEX_NAME);
    }
}
