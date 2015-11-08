package pt.blah.shopper.shops;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pt.blah.shopper.R;
import pt.blah.shopper.utils.Utilities;

public class ShopsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.shops_container, new ShopsFragment())
                    .commit();
        }

        Utilities.init(this);
    }

}
