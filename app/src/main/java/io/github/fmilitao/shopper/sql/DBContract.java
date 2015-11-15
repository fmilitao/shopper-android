package io.github.fmilitao.shopper.sql;


import android.provider.BaseColumns;

public interface DBContract {
    // note in SQLite 'false' is '0' and 'true' is '1'

    String DATABASE_NAME = "shopper.db";
    int DATABASE_VERSION = 14;

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

    //
    // Queries
    //

    interface JoinShopItemQuery extends BaseColumns {
        String COLUMN_NAME = "join_name";
        String COLUMN_ALL_ITEMS_COUNT = "all_items_count";
        String COLUMN_DONE_ITEMS_COUNT = "done_items_count";
        String COLUMN_NOT_DONE_ITEMS_COUNT = "not_done_items_count";

        // query
        String QUERY = "SELECT " +
                // shop_id
                ("L."+ ShopEntry._ID) + " AS " + JoinShopItemQuery._ID + ", " +
                // shop_name
                ("L."+ ShopEntry.COLUMN_SHOP_NAME) + " AS " + JoinShopItemQuery.COLUMN_NAME +", " +
                // all_items_count
                "COUNT( R." + ItemEntry._ID + " ) AS " + JoinShopItemQuery.COLUMN_ALL_ITEMS_COUNT + ", " +
                // items_done_sum
                "IFNULL( SUM( R." + ItemEntry.COLUMN_ITEM_DONE +" ), 0 ) AS " + JoinShopItemQuery.COLUMN_DONE_ITEMS_COUNT +", " +
                // items_not_done_sume
                "IFNULL( SUM( NOT R." + ItemEntry.COLUMN_ITEM_DONE +" ), 0 ) AS " + JoinShopItemQuery.COLUMN_NOT_DONE_ITEMS_COUNT +
                " FROM " +
                // shops table where not deleted
                "(SELECT * FROM " + ShopEntry.TABLE_NAME+ " WHERE "+ ShopEntry.COLUMN_DELETED + " = 0) L"+
                " LEFT JOIN " +
                // items table where not deleted
                "(SELECT * FROM " + ItemEntry.TABLE_NAME+ " WHERE "+ ItemEntry.COLUMN_DELETED + " = 0) R"+
                " ON " +
                ("L."+ ShopEntry._ID) +" = " + ("R."+ ItemEntry.COLUMN_ITEM_SHOP_ID_FK) +
                " GROUP BY " + ("L."+ ShopEntry._ID)+" ;";

        // indexes, assumed using the same order as the query above
        int INDEX_ID = 0;
        int INDEX_NAME = 1;
        int INDEX_ALL_ITEMS_COUNT = 2;
        int INDEX_DONE_ITEMS_COUNT = 3;
        int INDEX_NOT_DONE_ITEMS_COUNT = 4;
    }


    interface SelectItemQuery { //FIXME: SelectShopItemsQuery
        String QUERY = "SELECT " +
                ItemEntry._ID+", "+
                ItemEntry.COLUMN_ITEM_NAME+", "+
                ItemEntry.COLUMN_ITEM_QUANTITY+", "+
                ItemEntry.COLUMN_ITEM_DONE+", "+
                ItemEntry.COLUMN_ITEM_UNIT +", "+
                ItemEntry.COLUMN_ITEM_CATEGORY +" "+
                " FROM " + ItemEntry.TABLE_NAME + " WHERE " +
                ItemEntry.COLUMN_DELETED + " = 0 AND " +
                ItemEntry.COLUMN_ITEM_SHOP_ID_FK + "=? ORDER BY " +
                    ItemEntry.COLUMN_ITEM_DONE + ", " +
                    // only sorts by category if not done
                    " CASE WHEN "+ItemEntry.COLUMN_ITEM_DONE+" = 0 THEN " + ItemEntry.COLUMN_ITEM_CATEGORY + " END, "+
                    ItemEntry.COLUMN_ITEM_NAME + " COLLATE NOCASE ;";

        // indexes of query above, if order above changes so must the values below
        int INDEX_ID = 0;
        int INDEX_NAME = 1;
        int INDEX_QUANTITY = 2;
        int INDEX_IS_DONE = 3;
        int INDEX_UNIT = 4;
        int INDEX_CATEGORY = 5;
    }

    interface ShopsQuery {
        String QUERY = "SELECT " +
                ShopEntry._ID+", "+
                ShopEntry.COLUMN_SHOP_NAME +
                " FROM  " + ShopEntry.TABLE_NAME + " WHERE " + ShopEntry.COLUMN_DELETED+ " = 0 ;";

        // indexes of query above, if order above changes so must the values below
        int INDEX_ID = 0;
        int INDEX_NAME = 1;
    }

    interface UnitsQuery {
        String QUERY = "SELECT DISTINCT " +
                ItemEntry.COLUMN_ITEM_UNIT+
                " FROM  " + ItemEntry.TABLE_NAME +
                " WHERE " + ItemEntry.COLUMN_ITEM_UNIT + " IS NOT NULL "+
                " ORDER BY "+ItemEntry.COLUMN_ITEM_UNIT+" ;";

        int INDEX_NAME = 0;
    }

    interface CategoryQuery {
        String QUERY = "SELECT DISTINCT " +
                ItemEntry.COLUMN_ITEM_CATEGORY+
                " FROM  " + ItemEntry.TABLE_NAME +
                " WHERE " + ItemEntry.COLUMN_ITEM_CATEGORY + " IS NOT NULL "+
                " ORDER BY "+ItemEntry.COLUMN_ITEM_CATEGORY +" ;";

        int INDEX_NAME = 0;
    }

    //
    // creation queries
    //

    String CREATE_SHOPS = "CREATE TABLE " + ShopEntry.TABLE_NAME + " ( " +
            ShopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ShopEntry.COLUMN_SHOP_NAME+ " TEXT NOT NULL, " +
            ShopEntry.COLUMN_DELETED + " BOOLEAN NOT NULL " +
            " );";

    String CREATE_ITEMS = "CREATE TABLE " + ItemEntry.TABLE_NAME + " ( " +
            ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ItemEntry.COLUMN_ITEM_SHOP_ID_FK + " INTEGER NOT NULL, " +
            ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
            ItemEntry.COLUMN_ITEM_QUANTITY + " REAL NOT NULL, " +
            ItemEntry.COLUMN_ITEM_DONE + " BOOLEAN NOT NULL, " +
            ItemEntry.COLUMN_DELETED + " BOOLEAN NOT NULL, " +
            ItemEntry.COLUMN_ITEM_UNIT + " TEXT, " +
            ItemEntry.COLUMN_ITEM_CATEGORY + " TEXT, " +
            "FOREIGN KEY (" + ItemEntry.COLUMN_ITEM_SHOP_ID_FK + ") REFERENCES " +
            ShopEntry.TABLE_NAME + " (" + ShopEntry._ID + ") " +
            " );";

}
