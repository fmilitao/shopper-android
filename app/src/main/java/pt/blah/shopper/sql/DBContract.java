package pt.blah.shopper.sql;


import android.provider.BaseColumns;

public interface DBContract {

    String DATABASE_NAME = "shopper.db";
    int DATABASE_VERSION = 11;

    interface ShopEntry extends BaseColumns {
        String TABLE_NAME = "shops";

        String COLUMN_SHOP_NAME = "shop_name";
    }

    interface ItemEntry extends BaseColumns {
        String TABLE_NAME = "items";

        String COLUMN_ITEM_NAME = "item_name";
        String COLUMN_ITEM_QUANTITY = "item_quantity";
        String COLUMN_ITEM_DONE = "item_done";

        String COLUMN_ITEM_SHOP_ID_FK = "shop_id";
    }

    interface JoinShopItemQuery extends BaseColumns {
        String COLUMN_NAME = "join_name";
        String COLUMN_ALL_ITEMS_COUNT = "all_items_count";
        String COLUMN_DONE_ITEMS_COUNT = "done_items_count";
        String COLUMN_NOT_DONE_ITEMS_COUNT = "not_done_items_count";

        // query
        String QUERY = "SELECT " +
                (ShopEntry.TABLE_NAME+"."+ ShopEntry._ID) + " AS " + JoinShopItemQuery._ID + ", " +
                ShopEntry.COLUMN_SHOP_NAME + " AS " + JoinShopItemQuery.COLUMN_NAME +", " +
                "COUNT( " + (ItemEntry.TABLE_NAME+"."+ ItemEntry._ID) + " ) AS " + JoinShopItemQuery.COLUMN_ALL_ITEMS_COUNT + ", " +
                "IFNULL( SUM( " + ItemEntry.COLUMN_ITEM_DONE +" ), 0 ) AS " + JoinShopItemQuery.COLUMN_DONE_ITEMS_COUNT +", " +
                "IFNULL( SUM( NOT " + ItemEntry.COLUMN_ITEM_DONE +" ), 0 ) AS " + JoinShopItemQuery.COLUMN_NOT_DONE_ITEMS_COUNT +
                " FROM " + ShopEntry.TABLE_NAME+ " LEFT JOIN " + ItemEntry.TABLE_NAME +" ON " +
                (ShopEntry.TABLE_NAME+"."+ ShopEntry._ID) +"  = " + (ItemEntry.TABLE_NAME+"."+ ItemEntry.COLUMN_ITEM_SHOP_ID_FK) +
                " GROUP BY " + (ShopEntry.TABLE_NAME+"."+ ShopEntry._ID)+" ;";

        // indexes, assumed using the same order as the query above
        int INDEX_ID = 0;
        int INDEX_NAME = 1;
        int INDEX_ALL_ITEMS_COUNT = 2;
        int INDEX_DONE_ITEMS_COUNT = 3;
        int INDEX_NOT_DONE_ITEMS_COUNT = 4;
    }


    interface SelectItemQuery {
        String QUERY = "SELECT " +
                ItemEntry._ID+", "+
                ItemEntry.COLUMN_ITEM_NAME+", "+
                ItemEntry.COLUMN_ITEM_QUANTITY+", "+
                ItemEntry.COLUMN_ITEM_DONE+" "+
                " FROM " + ItemEntry.TABLE_NAME + " WHERE " +
                ItemEntry.COLUMN_ITEM_SHOP_ID_FK + "=? ORDER BY " +
                ItemEntry.COLUMN_ITEM_DONE + ", " + ItemEntry.COLUMN_ITEM_DONE + ";";

        // indexes of query above, if order above changes so must the values below
        int INDEX_ID = 0;
        int INDEX_NAME = 1;
        int INDEX_QUANTITY = 2;
        int INDEX_IS_DONE = 3;
    }

    //
    // creation queries
    //

    String CREATE_SHOPS = "CREATE TABLE " + ShopEntry.TABLE_NAME + " ( " +
            ShopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ShopEntry.COLUMN_SHOP_NAME+ " TEXT NOT NULL" +
            " );";

    String CREATE_ITEMS = "CREATE TABLE " + ItemEntry.TABLE_NAME + " ( " +
            ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ItemEntry.COLUMN_ITEM_SHOP_ID_FK + " INTEGER NOT NULL, " +
            ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
            ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, " +
            ItemEntry.COLUMN_ITEM_DONE + " BOOLEAN NOT NULL, " +
            "FOREIGN KEY (" + ItemEntry.COLUMN_ITEM_SHOP_ID_FK + ") REFERENCES " +
            ShopEntry.TABLE_NAME + " (" + ShopEntry._ID + ") " +
            " );";

}
