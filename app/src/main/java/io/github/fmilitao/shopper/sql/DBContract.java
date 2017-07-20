package io.github.fmilitao.shopper.sql;

import android.provider.BaseColumns;

public interface DBContract {
    // note in SQLite 'false' is '0' and 'true' is '1'

    String DATABASE_NAME = "shopper.db";

    int DATABASE_VERSION = 16;

    interface ShopEntry extends BaseColumns {
        String TABLE_NAME = "shops";

        String COLUMN_SHOP_NAME = "shop_name";
        String COLUMN_DELETED = "deleted";
    }

    interface ItemEntry extends BaseColumns {
        String TABLE_NAME = "items";

        String COLUMN_ITEM_NAME = "item_name";
        String COLUMN_ITEM_QUANTITY = "item_quantity";
        String COLUMN_ITEM_DONE = "item_done";
        String COLUMN_ITEM_SHOP_ID_FK = "shop_id";
        String COLUMN_DELETED = "deleted";
        String COLUMN_ITEM_UNIT = "item_unit";
        String COLUMN_ITEM_CATEGORY = "item_category";
    }

    String CREATE_SHOPS = "CREATE TABLE " + ShopEntry.TABLE_NAME +
            " ( " +
            ShopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ShopEntry.COLUMN_SHOP_NAME + " TEXT NOT NULL, " +
            ShopEntry.COLUMN_DELETED + " BOOLEAN NOT NULL " +
            " );";

    String CREATE_ITEMS = "CREATE TABLE " + ItemEntry.TABLE_NAME +
            " ( " +
            ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ItemEntry.COLUMN_ITEM_SHOP_ID_FK + " INTEGER NOT NULL, " +
            ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
            ItemEntry.COLUMN_ITEM_QUANTITY + " REAL NOT NULL, " +
            ItemEntry.COLUMN_ITEM_DONE + " BOOLEAN NOT NULL, " +
            ItemEntry.COLUMN_DELETED + " BOOLEAN NOT NULL, " +
            ItemEntry.COLUMN_ITEM_UNIT + " TEXT, " +
            ItemEntry.COLUMN_ITEM_CATEGORY + " TEXT, " +
            // reference to shop table
            "FOREIGN KEY (" + ItemEntry.COLUMN_ITEM_SHOP_ID_FK + ") REFERENCES " +
            ShopEntry.TABLE_NAME + " (" + ShopEntry._ID + ") " +
            "ON DELETE CASCADE" +
            " );";

}
