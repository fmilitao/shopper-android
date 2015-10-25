package pt.blah.shopper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

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

        Utilities.file = new File(this.getFilesDir(), Utilities.FILENAME);
        Utilities.context = getApplicationContext();

        Utilities.load();
    }

}
