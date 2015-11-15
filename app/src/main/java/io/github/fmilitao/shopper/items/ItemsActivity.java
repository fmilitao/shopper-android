package io.github.fmilitao.shopper.items;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import io.github.fmilitao.shopper.R;
import io.github.fmilitao.shopper.utils.UtilColors;

public class ItemsActivity extends AppCompatActivity {

    static public final String INTENT_SHOP_ID_LONG = "SHOP_ID";
    static public final String INTENT_SHOP_NAME_STRING = "SHOP_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_activity);
        
        // this initialization must be done *before* any list is created
        UtilColors.init(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.product_container, new ItemsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
}