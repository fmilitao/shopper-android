package pt.blah.shopper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ProductsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.product_container, new ProductsFragment())
                    .commit();
        }
    }

}