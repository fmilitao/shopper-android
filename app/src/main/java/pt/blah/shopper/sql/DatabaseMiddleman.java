package pt.blah.shopper.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        ContentValues v = new ContentValues();
        v.put(ShopEntry.COLUMN_SHOP_NAME, name);
        return mDb.insert(ShopEntry.TABLE_NAME, null, v);
    }

    public long createProduct(String name, long shopId, int quantity, boolean done) {
        ContentValues v = new ContentValues();
        v.put(ItemEntry.COLUMN_ITEM_NAME, name);
        v.put(ItemEntry.COLUMN_ITEM_SHOP_ID_FK, shopId);
        v.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        v.put(ItemEntry.COLUMN_ITEM_DONE, done);
        return mDb.insert(ItemEntry.TABLE_NAME, null, v);
    }

    public boolean deleteAll() {
        int doneDelete;

        doneDelete = mDb.delete(ItemEntry.TABLE_NAME, null, null);
        Log.w(TAG, Integer.toString(doneDelete));

        doneDelete = mDb.delete(ShopEntry.TABLE_NAME, null, null);
        Log.w(TAG, Integer.toString(doneDelete));

        return doneDelete > 0;
    }

    public Cursor fetchAllShops() {
        Log.w(TAG, JoinShopItemQuery.QUERY);

        Cursor mCursor = mDb.rawQuery(JoinShopItemQuery.QUERY, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    public Cursor fetchAllItems(String shopId) {
        Log.w(TAG, SelectItemQuery.QUERY);

        Cursor mCursor = mDb.rawQuery(SelectItemQuery.QUERY, new String[]{shopId});

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    public long insertSomeValues() {

        long id;
        long first;

        first = id = createShop("Jumbo");
        createProduct("Bananas", id, 10, false);
        createProduct("Batatas", id, 2, false);
        createProduct("Peixe", id, 7, true);

        id = createShop("LIDL");
        createProduct("Queijo", id, 11, false);
        createProduct("Leite", id, 22, false);
        createProduct("Pao", id, 1, false);
        createProduct("Manteiga", id, 1, false);

        id = createShop("Continente");
        id = createShop("ALDI");
        id = createShop("Test 123");

        return first;
    }

    public boolean deleteShop(String rowId) {
        Log.w(TAG, "delete " + rowId);
        return mDb.delete(ShopEntry.TABLE_NAME, ShopEntry._ID + "=" + rowId, null) > 0;
    }

    public boolean flipItem(long rowid, int old) {
        Log.w(TAG, "update " + rowid);

        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_DONE, old == 0 ? 1 : 0);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + rowid, null) > 0;
    }


    public void printAll(){
        // TODO to define an iterable we must also define a proper wrapper for Cursor.
        Cursor c = fetchAllShops();
        c.moveToFirst();

        do{
            Log.w(TAG, " >> " + c.getString(JoinShopItemQuery.INDEX_NAME) );
        }while( c.moveToNext() );

        Log.w(TAG, " << ");

    }

}