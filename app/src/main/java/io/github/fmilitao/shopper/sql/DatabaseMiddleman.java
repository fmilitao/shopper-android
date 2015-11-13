package io.github.fmilitao.shopper.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import io.github.fmilitao.shopper.sql.DBContract.ItemEntry;
import io.github.fmilitao.shopper.sql.DBContract.JoinShopItemQuery;
import io.github.fmilitao.shopper.sql.DBContract.SelectItemQuery;
import io.github.fmilitao.shopper.sql.DBContract.ShopEntry;
import io.github.fmilitao.shopper.sql.DBContract.TransferItemQuery;
import io.github.fmilitao.shopper.sql.DBContract.ShopsQuery;

//TODO: consider protecting against sql injections.
public class DatabaseMiddleman {

    private static final String TAG = DatabaseMiddleman.class.toString();

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    public DatabaseMiddleman(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        if( mDb == null && mDbHelper == null ) {
            mDbHelper = new DatabaseHelper(mCtx);
            // always uses the same writable database, even when reading
            // FIXME: this should be moved off the main thread, use AsyncTask?
            mDb = mDbHelper.getWritableDatabase();
        }
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
            mDb.close();
            mDb = null;
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

        doneDelete += mDb.delete(ShopEntry.TABLE_NAME, null, null);
        Log.v(TAG, Integer.toString(doneDelete));

        return doneDelete > 0;
    }

    public Cursor fetchAllShops() {
        Log.v(TAG, JoinShopItemQuery.QUERY);

        Cursor c = mDb.rawQuery(JoinShopItemQuery.QUERY, null);

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor fetchShopItems(long shopId) {
        Log.v(TAG, SelectItemQuery.QUERY);

        Cursor c = mDb.rawQuery(SelectItemQuery.QUERY, new String[]{Long.toString(shopId)});

        if (c != null) {
            c.moveToFirst();
        }
        return c;
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

    public boolean flipItem(long itemId, int old) {
        Log.v(TAG, "update " + itemId);

        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_DONE, old == 0 ? 1 : 0);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
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
        c.close();

        return builder.toString();
    }

    public boolean transfer(long[] itemIds, long toShopId){
        Log.v(TAG, TransferItemQuery.QUERY);

        for(long id : itemIds ) {
            Cursor c = mDb.rawQuery(TransferItemQuery.QUERY, new String[]{Long.toString(id)});

            if( c == null )
                continue;

            c.moveToFirst();

            createItem(
                    c.getString(TransferItemQuery.INDEX_NAME),
                    toShopId,
                    c.getInt(TransferItemQuery.INDEX_QUANTITY),
                    // recall 0 is false, 1 is true
                    c.getInt(TransferItemQuery.INDEX_IS_DONE) == 1
            );

            // marks transferred item as deleted
            updateItemDeleted(id, true);

            c.close();
        }

        return true;
    }

    public Pair<Long,String>[] makeAllShopPair(){
        Cursor c = mDb.rawQuery(ShopsQuery.QUERY, null);
        c.moveToFirst();

        @SuppressWarnings("unchecked")
        Pair<Long,String>[] res = new Pair[c.getCount()];
        int i=0;
        do{
            res[i++] = new Pair<>(
                    c.getLong(ShopsQuery.INDEX_ID),
                    c.getString(ShopsQuery.INDEX_NAME)
            );
        }while( c.moveToNext() );
        c.close();

        return res;
    }

    public boolean renameShop(long shopId, String newName) {
        ContentValues args = new ContentValues();
        args.put(ShopEntry.COLUMN_SHOP_NAME, newName);

        Log.v(TAG, " update: " + shopId + " "+ newName );

        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    public boolean updateItem(long itemId, String itemName, int itemQuantity) {
        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_NAME, itemName);
        args.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
    }

    public boolean updateShopDeleted(long shopId, boolean value) {
        ContentValues args = new ContentValues();
        args.put(ShopEntry.COLUMN_DELETED, value);
        Log.v(TAG, " deleted: " + shopId+ " << " + value);
        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    public boolean updateItemDeleted(long itemId, boolean value) {
        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_DELETED, value);
        Log.v(TAG, " deleted: " + itemId+ " << " + value);
        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
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

        createShop("Continente");
        createShop("ALDI");
        createShop("Test 123");

        return first;
    }

    //
    // I/O storing
    //

    public void saveShopItems(PrintWriter out,long shopId){
        Cursor c = fetchShopItems(shopId);
        if( c == null )
            return;
        c.moveToFirst();
        do{
            out.println(
                    // removes any chance of collision with ','
                    c.getString(SelectItemQuery.INDEX_NAME).replace(',', ' ')
                    + "," +
                    c.getString(SelectItemQuery.INDEX_QUANTITY)
                    + "," +
                    Boolean.toString( c.getInt(SelectItemQuery.INDEX_IS_DONE) != 0 )
            );

        }while( c.moveToNext() );
        c.close();
    }

    public void loadShopItems(Scanner sc, long shopId, Set<Long> set){
        sc.useDelimiter("(,)|(\\n+)");
        while( sc.hasNext() ) {
            String name = sc.next();
            int quantity = Integer.parseInt(sc.next().trim());
            boolean isDone = Boolean.parseBoolean( sc.next().trim() );
            long res = createItem(name,shopId,quantity,isDone);
            if( res != -1 )
                set.add(res);
        }
    }

    public void loadShopItems(List<Pair<String,Integer>> lst, long shopId, Set<Long> set){
        for(Pair<String,Integer> p : lst ){
            String name = p.first;
            int quantity = p.second;
            long res = createItem(name,shopId,quantity,false);
            if( res != -1 )
                set.add(res);
        }
    }

}