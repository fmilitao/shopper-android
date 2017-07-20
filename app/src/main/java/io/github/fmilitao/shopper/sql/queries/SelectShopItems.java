package io.github.fmilitao.shopper.sql.queries;

import android.database.Cursor;

import io.github.fmilitao.shopper.sql.DBContract;

public class SelectShopItems {
    public static final String QUERY = "SELECT " +
            DBContract.ItemEntry._ID+", "+
            DBContract.ItemEntry.COLUMN_ITEM_NAME+", "+
            DBContract.ItemEntry.COLUMN_ITEM_QUANTITY+", "+
            DBContract.ItemEntry.COLUMN_ITEM_DONE+", "+
            DBContract.ItemEntry.COLUMN_ITEM_UNIT +", "+
            DBContract.ItemEntry.COLUMN_ITEM_CATEGORY +" "+
            " FROM " + DBContract.ItemEntry.TABLE_NAME + " WHERE " +
            DBContract.ItemEntry.COLUMN_DELETED + " = 0 AND " +
            DBContract.ItemEntry.COLUMN_ITEM_SHOP_ID_FK + "=? ORDER BY " +
            DBContract.ItemEntry.COLUMN_ITEM_DONE + ", " +
            // only sorts by category if not done
            " CASE WHEN "+ DBContract.ItemEntry.COLUMN_ITEM_DONE+" = 0 THEN " + DBContract.ItemEntry.COLUMN_ITEM_CATEGORY + " END, "+
            DBContract.ItemEntry.COLUMN_ITEM_NAME + " COLLATE NOCASE ;";

    // indexes of query above, if order above changes so must the values below
    private static final int INDEX_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_QUANTITY = 2;
    private static final int INDEX_IS_DONE = 3;
    private static final int INDEX_UNIT = 4;
    private static final int INDEX_CATEGORY = 5;

    public static long getId(Cursor c){
        return c.getLong(INDEX_ID);
    }

    public static String getName(Cursor c){
        return c.getString(INDEX_NAME);
    }

    public static float getQuantity(Cursor c){
        return c.getFloat(INDEX_QUANTITY);
    }

    public static String getQuantityString(Cursor c){
        return c.getString(INDEX_QUANTITY);
    }

    public static boolean isDone(Cursor c){
        return c.getInt(INDEX_IS_DONE) != 0;
    }

    public static int getIsDone(Cursor c){
        return c.getInt(INDEX_IS_DONE);
    }

    public static String getUnit(Cursor c){
        return c.getString(INDEX_UNIT);
    }

    public static String getCategory(Cursor c){
        return c.getString(INDEX_CATEGORY);
    }
}
