package pt.blah.shopper.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import java.util.List;

import pt.blah.shopper.sql.DBContract.ItemEntry;
import pt.blah.shopper.sql.DBContract.JoinShopItemQuery;
import pt.blah.shopper.sql.DBContract.SelectItemQuery;
import pt.blah.shopper.sql.DBContract.ShopEntry;

// TODO undo/redo action with delete column.
public class DatabaseMiddleman {

    private static final String TAG = DatabaseMiddleman.class.toString();

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    public DatabaseMiddleman(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        // always uses the same writable database, even when reading
        // FIXME this should be moved off the main thread, use AsyncTask?
        mDb = mDbHelper.getWritableDatabase();
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    public long createShop(String name) {
        return createShop(name,null);
    }

    public long createShop(String name, List<Pair<String,Integer>> items) {
        ContentValues v = new ContentValues();
        v.put(ShopEntry.COLUMN_SHOP_NAME, name);
        v.put(ShopEntry.COLUMN_DELETED, false);
        long shopId = mDb.insert(ShopEntry.TABLE_NAME, null, v);

        // creates items for the shop if provided
        if( items != null ) {
            for (Pair<String, Integer> item : items) {
                createItem(item.first, shopId, item.second, false);
            }
        }

        return shopId;
    }

    public long createItem(String name, long shopId, int quantity, boolean done) {
        ContentValues v = new ContentValues();
        v.put(ItemEntry.COLUMN_ITEM_NAME, name);
        v.put(ItemEntry.COLUMN_ITEM_SHOP_ID_FK, shopId);
        v.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        v.put(ItemEntry.COLUMN_ITEM_DONE, done);
        v.put(ItemEntry.COLUMN_DELETED, false);
        return mDb.insert(ItemEntry.TABLE_NAME, null, v);
    }

    public boolean deleteAll() {
        int doneDelete;

        doneDelete = mDb.delete(ItemEntry.TABLE_NAME, null, null);
        Log.v(TAG, Integer.toString(doneDelete));

        doneDelete = mDb.delete(ShopEntry.TABLE_NAME, null, null);
        Log.v(TAG, Integer.toString(doneDelete));

        return doneDelete > 0;
    }

    public Cursor fetchAllShops() {
        // FIXME: this requires garbage collecting items of the shop or numbers will be wrong.

        Log.v(TAG, JoinShopItemQuery.QUERY);

        Cursor mCursor = mDb.rawQuery(JoinShopItemQuery.QUERY, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchShopItems(long shopId) {
        Log.v(TAG, SelectItemQuery.QUERY);

        Cursor mCursor = mDb.rawQuery(SelectItemQuery.QUERY, new String[]{Long.toString(shopId)});

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean gcShops() {
        int count = mDb.delete(ShopEntry.TABLE_NAME, ShopEntry.COLUMN_DELETED + "= 1", null);
        Log.v(TAG, "shops.gc=" + count);
        return count > 0;
    }

    public boolean gcItems() {
        int count = mDb.delete(ItemEntry.TABLE_NAME, ItemEntry.COLUMN_DELETED + "= 1", null);
        Log.v(TAG, "items.gc=" + count);
        return count > 0;
    }

    public boolean flipItem(long rowid, int old) {
        Log.v(TAG, "update " + rowid);

        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_DONE, old == 0 ? 1 : 0);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + rowid, null) > 0;
    }

    public String stringifyItemList(long shopId){
        Cursor c = fetchShopItems(shopId);
        c.moveToFirst();

        StringBuilder builder = new StringBuilder();
        do{
            builder.append(c.getString(SelectItemQuery.INDEX_NAME));
            builder.append(" ");
            builder.append(c.getString(SelectItemQuery.INDEX_QUANTITY));
            builder.append("\n");
        }while( c.moveToNext() );

        return builder.toString();
    }

    public boolean renameShop(long shopId, String newName) {
        ContentValues args = new ContentValues();
        args.put(ShopEntry.COLUMN_SHOP_NAME, newName);

        Log.v(TAG, " update: " + shopId + " "+ newName );

        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    public boolean updateShopDeleted(long shopId, boolean value) {
        ContentValues args = new ContentValues();
        args.put(ShopEntry.COLUMN_DELETED, value);
        Log.v(TAG, " deleted: " + shopId+ " << " + value);
        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    //
    // Populate Tables
    //

    public long insertSomeValues() {

        long id;
        long first;

        first = id = createShop("Jumbo");
        createItem("Bananas", id, 10, false);
        createItem("Batatas", id, 2, false);
        createItem("Peixe", id, 7, true);

        id = createShop("LIDL");
        createItem("Queijo", id, 11, false);
        createItem("Leite", id, 22, false);
        createItem("Pao", id, 1, false);
        createItem("Manteiga", id, 1, false);

        id = createShop("Continente");
        id = createShop("ALDI");
        id = createShop("Test 123");

        return first;
    }

}