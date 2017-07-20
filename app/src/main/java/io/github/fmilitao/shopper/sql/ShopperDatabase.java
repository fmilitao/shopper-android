package io.github.fmilitao.shopper.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.fmilitao.shopper.sql.DBContract.ItemEntry;
import io.github.fmilitao.shopper.sql.DBContract.ShopEntry;
import io.github.fmilitao.shopper.sql.queries.JoinShopsItems;
import io.github.fmilitao.shopper.sql.queries.SelectDistinctCategories;
import io.github.fmilitao.shopper.sql.queries.SelectDistinctItemNames;
import io.github.fmilitao.shopper.sql.queries.SelectDistinctUnits;
import io.github.fmilitao.shopper.sql.queries.SelectShopItems;
import io.github.fmilitao.shopper.sql.queries.SelectShopItemsQuantities;
import io.github.fmilitao.shopper.sql.queries.SelectShops;
import io.github.fmilitao.shopper.utils.UtilItemCsv;
import io.github.fmilitao.shopper.utils.model.Item;
import io.github.fmilitao.shopper.utils.model.Shop;

public class ShopperDatabase extends DatabaseHelper {

    private static final String TAG = ShopperDatabase.class.getCanonicalName();

    private final Configuration mConfiguration;

    /**
     * Holds default values for different columns.
     * These default values may not be statically known.
     */
    public static class Configuration {
        final String noneUnit;
        final String defaultCategory;
        final String nullString;

        public Configuration(String noneUnit, String defaultCategory, String nullString) {
            this.noneUnit = noneUnit;
            this.defaultCategory = defaultCategory;
            this.nullString = nullString;
        }
    }

    public ShopperDatabase(Context context, Configuration configuration) {
        super(context);
        this.mConfiguration = configuration;
    }

    //
    // Shops
    //

    public long createShop(String name) {
        return createShop(name, new LinkedList<Item>());
    }

    /**
     * @return the ID of the newly inserted shop, or -1 if an error occurred
     */
    public long createShop(String name, List<Item> items) {
        ContentValues newValues = new ContentValues();
        newValues.put(ShopEntry.COLUMN_SHOP_NAME, name);
        newValues.put(ShopEntry.COLUMN_DELETED, false);

        long shopId = mDb.insert(ShopEntry.TABLE_NAME, null, newValues);

        // adds the provided items
        if (items != null) {
            for (Item item : items) {
                // note that item id is intentionally ignored here
                if (createItem(item.getName(), shopId, item.getQuantity(), false, item.getUnit(), item.getCategory()) == -1) {
                    Log.e(TAG, "Failed to add item: " + item.getName());
                }
            }
        }

        return shopId;
    }

    /**
     * @return the number of rows affected TODO! there should be only one!
     */
    public boolean renameShop(long shopId, String newName) {
        ContentValues args = new ContentValues();
        args.put(ShopEntry.COLUMN_SHOP_NAME, newName);

        Log.v(TAG, " update: " + shopId + " " + newName);

        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    public Cursor fetchAllShops() {
        Log.v(TAG, JoinShopsItems.QUERY);

        Cursor c = mDb.rawQuery(JoinShopsItems.QUERY, null);

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

    public boolean updateShopDeleted(long shopId, boolean value) {
        ContentValues args = new ContentValues();
        args.put(ShopEntry.COLUMN_DELETED, value);
        Log.v(TAG, " deleted: " + shopId + " << " + value);
        return mDb.update(ShopEntry.TABLE_NAME, args, ShopEntry._ID + "=" + shopId, null) > 0;
    }

    //
    // Items
    //

    public long createItem(String name, long shopId, double quantity, boolean done, String unit, String category) {
        if (unit != null && (mConfiguration.noneUnit.equalsIgnoreCase(unit) || unit.length() <= 0))
            unit = null;
        if (category != null && (mConfiguration.defaultCategory.equalsIgnoreCase(category) || category.length() <= 0))
            category = null;

        ContentValues v = new ContentValues();
        v.put(ItemEntry.COLUMN_ITEM_NAME, name);
        v.put(ItemEntry.COLUMN_ITEM_SHOP_ID_FK, shopId);
        v.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        v.put(ItemEntry.COLUMN_ITEM_DONE, done);
        v.put(ItemEntry.COLUMN_DELETED, false);
        v.put(ItemEntry.COLUMN_ITEM_UNIT, unit);
        v.put(ItemEntry.COLUMN_ITEM_CATEGORY, category);
        return mDb.insert(ItemEntry.TABLE_NAME, null, v);
    }

    public Cursor fetchShopItems(long shopId) {
        Log.v(TAG, SelectShopItems.QUERY);

        Cursor c = mDb.rawQuery(SelectShopItems.QUERY, new String[]{Long.toString(shopId)});

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor fetchShopDetails(long shopId) {
        Log.v(TAG, SelectShopItemsQuantities.QUERY);

        Cursor c = mDb.rawQuery(SelectShopItemsQuantities.QUERY, new String[]{Long.toString(shopId)});

        if (c != null) {
            c.moveToFirst();
        }
        return c;
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

    private List<Item> getShopItems(long shopId) {
        Cursor c = fetchShopItems(shopId);
        c.moveToFirst();

        List<Item> itemList = new ArrayList<>(c.getCount());
        do {
            itemList.add(
                    new Item(
                            SelectShopItems.getId(c),
                            SelectShopItems.getName(c),
                            SelectShopItems.getUnit(c),
                            SelectShopItems.getQuantity(c),
                            SelectShopItems.getCategory(c),
                            SelectShopItems.isDone(c)
                    )
            );
        } while (c.moveToNext());
        c.close();

        return itemList;
    }

    public String stringifyItemList(long shopId) {
        try {
            return UtilItemCsv.ClipBoard.itemListToString(getShopItems(shopId));
        } catch (IOException error) {
            Log.e(TAG, "Error stringifying item list", error);
            return "";
        }
    }

    public Shop[] makeAllShopPair() {
        Cursor c = mDb.rawQuery(SelectShops.QUERY, null);
        c.moveToFirst();

        Shop[] res = new Shop[c.getCount()];
        int i = 0;
        do {
            res[i++] = new Shop(
                    SelectShops.getId(c),
                    SelectShops.getName(c)
            );
        } while (c.moveToNext());
        c.close();

        return res;
    }

    public boolean updateItem(long itemId, String itemName, float itemQuantity, String unit, String category) {
        if (unit != null && (mConfiguration.noneUnit.equalsIgnoreCase(unit) || unit.length() <= 0))
            unit = null;
        if (category != null && (mConfiguration.defaultCategory.equalsIgnoreCase(category) || category.length() <= 0))
            category = null;

        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_NAME, itemName);
        args.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);
        args.put(ItemEntry.COLUMN_ITEM_UNIT, unit);
        args.put(ItemEntry.COLUMN_ITEM_CATEGORY, category);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
    }

    public boolean updateItemShopId(long itemId, long shopId) {
        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_ITEM_SHOP_ID_FK, shopId);

        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
    }

    public boolean updateItemDeleted(long itemId, boolean value) {
        ContentValues args = new ContentValues();
        args.put(ItemEntry.COLUMN_DELETED, value);
        Log.v(TAG, " deleted: " + itemId + " << " + value);
        return mDb.update(ItemEntry.TABLE_NAME, args, ItemEntry._ID + "=" + itemId, null) > 0;
    }

    //
    // Units
    //

    public String[] getAllUnits() {
        Cursor c = mDb.rawQuery(SelectDistinctUnits.QUERY, null);
        c.moveToFirst();

        String[] res;
        if (c.getCount() > 0) {
            res = new String[c.getCount() + 1];
            int i = 0;
            res[i++] = mConfiguration.noneUnit;
            do {
                res[i++] = SelectDistinctUnits.getName(c);
            } while (c.moveToNext());
        } else {
            res = new String[]{mConfiguration.noneUnit};
        }
        c.close();
        return res;
    }

    //
    // Item Names
    //

    public String[] getAllItemNames() {
        Cursor c = mDb.rawQuery(SelectDistinctItemNames.QUERY, null);
        c.moveToFirst();

        String[] res;
        if (c.getCount() > 0) {
            res = new String[c.getCount()];
            int i = 0;
            do {
                res[i++] = SelectDistinctItemNames.getName(c);
            } while (c.moveToNext());
        } else {
            res = new String[]{};
        }
        c.close();
        return res;
    }

    //
    // Categories
    //

    public String[] getAllCategories() {
        Cursor c = mDb.rawQuery(SelectDistinctCategories.QUERY, null);
        c.moveToFirst();

        String[] res;
        if (c.getCount() > 0) {
            res = new String[c.getCount() + 1];
            int i = 0;
            res[i++] = mConfiguration.defaultCategory;
            do {
                res[i++] = SelectDistinctCategories.getName(c);
            } while (c.moveToNext());
        } else {
            res = new String[]{mConfiguration.defaultCategory};
        }
        c.close();
        return res;
    }

    //
    // I/O storing
    //

    public void saveShopItems(PrintWriter out, long shopId) {
        try {
            out.println(UtilItemCsv.File.itemListToString(getShopItems(shopId)));
        } catch (IOException error) {
            Log.e(TAG, "Error stringifying item list", error);
        }
    }

    public void loadShopItems(File file, long shopId, Set<Long> set) {
        try {
            List<Item> itemList = UtilItemCsv.File.fileToItemList(file);
            loadShopItems(itemList, shopId, set);
        } catch (IOException error) {
            Log.e(TAG, "Error reading file " + file, error);
        }
    }

    public void loadShopItems(List<Item> itemList, long shopId, Set<Long> set) {
        for (Item item : itemList) {
            String unit = item.getUnit();
            if (mConfiguration.nullString.equalsIgnoreCase(unit)) {
                unit = null;
            }
            String category = item.getCategory();
            if (mConfiguration.nullString.equalsIgnoreCase(category)) {
                category = null;
            }
            long res = createItem(item.getName(), shopId, item.getQuantity(), item.isDone(), unit, category);
            if (res != -1) {
                set.add(res);
            }
        }
    }

}