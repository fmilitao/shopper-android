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
import io.github.fmilitao.shopper.sql.DBContract.ShopsQuery;
import io.github.fmilitao.shopper.utils.Utilities;

//TODO: consider protecting against sql injections.
public class DatabaseMiddleman {

    private static final String NONE = "<none>";
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

    public long createShop(String name, List<Utilities.Triple<String,Float,String>> items) {
        ContentValues v = new ContentValues();
        v.put(ShopEntry.COLUMN_SHOP_NAME, name);
        v.put(ShopEntry.COLUMN_DELETED, false);
        long shopId = mDb.insert(ShopEntry.TABLE_NAME, null, v);

        // creates items for the shop if provided
        if( items != null ) {
            for (Utilities.Triple<String,Float,String> item : items) {
                createItem(item.first, shopId, item.second, false, item.third);
            }
        }

        return shopId;
    }

    public long createItem(String name, long shopId, float quantity, boolean done, String unit) {
        if( unit != null && ( NONE.equalsIgnoreCase(unit) || unit.length() <= 0  ) )
            unit = null;

        ContentValues v = new ContentValues();
        v.put(ItemEntry.COLUMN_ITEM_NAME, name);
        v.put(ItemEntry.COLUMN_ITEM_SHOP_ID_FK, shopId);
        v.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        v.put(ItemEntry.COLUMN_ITEM_DONE, done);
        v.put(ItemEntry.COLUMN_DELETED, false);
        v.put(ItemEntry.COLUMN_ITEM_UNIT, unit);
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

        String unit;
        StringBuilder builder = new StringBuilder();
        do{
            builder.append(c.getString(SelectItemQuery.INDEX_NAME));
            builder.append(" ");
            builder.append(c.getString(SelectItemQuery.INDEX_QUANTITY));
            unit = c.getString(SelectItemQuery.INDEX_UNIT);
            if( unit != null ){
                builder.append(" ");
                builder.append(unit);
            }
            builder.append("\n");
        }while( c.moveToNext() );
        c.close();

        return builder.toString();
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

        Log.v(TAG, " update: " + shopId + " " + newName);

        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    public boolean updateItem(long itemId, String itemName, float itemQuantity, String unit) {
        if( unit != null && ( NONE.equalsIgnoreCase(unit) || unit.length() <= 0  ) )
            unit = null;

        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_NAME, itemName);
        args.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);
        args.put(ItemEntry.COLUMN_ITEM_UNIT, unit);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
    }

    public boolean updateItemShopId(long itemId, long shopId) {
        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_SHOP_ID_FK, shopId);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
    }

    public boolean updateShopDeleted(long shopId, boolean value) {
        ContentValues args = new ContentValues();
        args.put(ShopEntry.COLUMN_DELETED, value);
        Log.v(TAG, " deleted: " + shopId + " << " + value);
        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    public boolean updateItemDeleted(long itemId, boolean value) {
        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_DELETED, value);
        Log.v(TAG, " deleted: " + itemId+ " << " + value);
        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
    }

    public String[] getAllUnits(){
        Log.v(TAG, " Units: " + DBContract.UnitsQuery.QUERY);

        Cursor c = mDb.rawQuery(DBContract.UnitsQuery.QUERY, null);
        c.moveToFirst();

        String[] res;
        if( c.getCount() > 0 ) {
            res = new String[c.getCount() + 1];
            int i = 0;
            res[i++] = NONE;
            do {
                res[i++] = c.getString(DBContract.UnitsQuery.INDEX_NAME);
            } while (c.moveToNext());
        }else{
            res = new String[]{NONE};
        }
        c.close();
        return res;
    }

    //
    // Populate Tables
    //

    public long insertSomeValues() {

        long id;
        long first;

        first = id = createShop("Jumbo");
        createItem("Bananas", id, 10, false,null);
        createItem("Batatas", id, 2, false,null);
        createItem("Peixe", id, 7, true,null);

        id = createShop("LIDL");
        createItem("Queijo", id, 11, false,null);
        createItem("Leite", id, 22, false,null);
        createItem("Pao", id, 1, false,null);
        createItem("Manteiga", id, 1, false,null);

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
                    Boolean.toString(c.getInt(SelectItemQuery.INDEX_IS_DONE) != 0)
                    + "," +
                    c.getString(SelectItemQuery.INDEX_UNIT)
            );

        }while( c.moveToNext() );
        c.close();
    }

    public void loadShopItems(Scanner sc, long shopId, Set<Long> set){
        sc.useDelimiter("(,)|(\\n+)");
        while( sc.hasNext() ) {
            String name = sc.next();
            float quantity = Float.parseFloat(sc.next().trim());
            boolean isDone = Boolean.parseBoolean(sc.next().trim());
            String unit = sc.next();
            if( unit.equalsIgnoreCase("null") )
                unit = null;
            long res = createItem(name,shopId,quantity,isDone,unit);
            if( res != -1 )
                set.add(res);
        }
    }

    public void loadShopItems(List<Utilities.Triple<String,Float,String>> lst, long shopId, Set<Long> set){
        for(Utilities.Triple<String,Float,String> p : lst ){
            String name = p.first;
            float quantity = p.second;
            String unit = p.third;
            long res = createItem(name,shopId,quantity,false,unit);
            if( res != -1 )
                set.add(res);
        }
    }

}