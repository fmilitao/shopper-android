package io.github.fmilitao.shopper;

import android.database.Cursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.emory.mathcs.backport.java.util.Collections;
import io.github.fmilitao.shopper.BuildConfig;
import io.github.fmilitao.shopper.sql.ShopperDatabase;
import io.github.fmilitao.shopper.sql.queries.JoinShopsItems;
import io.github.fmilitao.shopper.sql.queries.SelectShopItems;
import io.github.fmilitao.shopper.sql.queries.SelectShopItemsQuantities;
import io.github.fmilitao.shopper.utils.model.Item;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.core.Is.is;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ShopperDatabaseTester {
    private ShopperDatabase shopperDb;

    private static final String NONE_UNIT = "<NONE>";
    private static final String DEFAULT_CATEGORY = "<DEFAULT>";
    private static final String NULL_STRING = "<NULL>";

    // mock values
    private static final String itemName = "test Item";
    private static final float quantity = 1.5f;
    private static final boolean isDone = false;
    private static final String unit = "metric tons";
    private static final String category = "meats";
    private static final String shopName = "valid shop";

    private static final ShopperDatabase.Configuration CONFIGURATION =
            new ShopperDatabase.Configuration(NONE_UNIT, DEFAULT_CATEGORY, NULL_STRING);

    @Before
    public void setUp() throws Exception {
        shopperDb = new ShopperDatabase(RuntimeEnvironment.application, CONFIGURATION);
        shopperDb.open();
    }

    @After
    public void tearDown() throws Exception {
        if (shopperDb != null) {
            shopperDb.close();
        }
    }

    @Test
    public void testShopCreation() {
        String testShopName = "testShopName";
        long validShopId = shopperDb.createShop(testShopName);

        // id cannot be -1 if the shop is valid
        assertThat(validShopId).isNotEqualTo(-1);

        Cursor cursor = shopperDb.fetchShopDetails(validShopId);

        assertThat(SelectShopItemsQuantities.getItemCount(cursor)).isEqualTo(0);
        assertThat(SelectShopItemsQuantities.getNotDoneItemCount(cursor)).isEqualTo(0);

        Cursor shops = shopperDb.fetchAllShops();

        // these should only exist one shop
        assertThat(shops.getCount()).isEqualTo(1);
        assertThat(JoinShopsItems.getId(shops)).isEqualTo(validShopId);
        assertThat(JoinShopsItems.getName(shops)).isEqualTo(testShopName);
        assertThat(JoinShopsItems.getNotDoneItemCount(shops)).isEqualTo(0);
        assertThat(JoinShopsItems.getDoneItemCount(shops)).isEqualTo(0);
        assertThat(JoinShopsItems.getItemCountString(shops)).isEqualTo("0");
    }

    @Test
    public void testItemCreation() {
        // check invalid item creation
        long invalidItemId = shopperDb.createItem(itemName, 234, quantity, isDone, unit, category);
        assertThat(invalidItemId).isEqualTo(-1);

        // check valid shop creation
        long validShopId = shopperDb.createShop(shopName);
        assertThat(validShopId).isNotEqualTo(-1);

        // check valid item creation
        long validItemId = shopperDb.createItem(itemName, validShopId, quantity, isDone, unit, category);
        assertThat(validItemId).isNotEqualTo(-1);

        // check correct item
        Cursor cursor = shopperDb.fetchShopItems(validShopId);
        assertThat(cursor.getCount()).isEqualTo(1);
        assertThat(SelectShopItems.getId(cursor)).isEqualTo(validItemId);
        assertThat(SelectShopItems.getName(cursor)).isEqualTo(itemName);
        assertThat(SelectShopItems.getCategory(cursor)).isEqualTo(category);
        assertThat(SelectShopItems.getUnit(cursor)).isEqualTo(unit);
        assertThat(SelectShopItems.getQuantity(cursor)).isEqualTo(quantity);
        assertThat(SelectShopItems.getQuantityString(cursor)).isEqualTo(Float.toString(quantity));
        assertThat(SelectShopItems.isDone(cursor)).isEqualTo(isDone);
        assertThat(SelectShopItems.getIsDone(cursor)).isEqualTo(isDone ? 1 : 0);
    }

    @Test
    public void testShopDeleteCascade() {
        int count = shopperDb.fetchAllShops().getCount();

        // check valid shop creation
        long validShopId = shopperDb.createShop(shopName);
        assertThat(validShopId).isNotEqualTo(-1);
        assertThat(shopperDb.fetchAllShops().getCount()).isEqualTo(count + 1);

        // check valid item creation
        long validItemId = shopperDb.createItem(itemName, validShopId, quantity, isDone, unit, category);
        assertThat(validItemId).isNotEqualTo(-1);

        Cursor cursor;

        // check correct item
        cursor = shopperDb.fetchShopItems(validShopId);
        assertThat(cursor.getCount()).isEqualTo(1);

        // delete shop
        assertThat(shopperDb.updateShopDeleted(validShopId, true)).isTrue();

        // should count as deleted even before gc
        assertThat(shopperDb.fetchAllShops().getCount()).isEqualTo(count);

        // force gc on shops
        shopperDb.gcShops();

        // assert that all existing items were deleted
        cursor = shopperDb.fetchShopItems(validShopId);
        assertThat(cursor.getCount()).isEqualTo(0);

        assertThat(shopperDb.fetchAllShops().getCount()).isEqualTo(count);
        assertThat(shopperDb.getAllItemNames()).isEmpty();
    }

    // TODO test delete item
    // TODO test delete shop

    @Test
    public void testGetAllUnits() {
        // TODO
//        shopperDb.getAllUnits();

    }

    @Test
    public void testGetAllCategories() {
        // TODO
        //        shopperDb.getAllCategories();
    }

    @Test
    public void testGetAllItemNames() {
        // TODO
        //        shopperDb.getAllItemNames();
    }
}