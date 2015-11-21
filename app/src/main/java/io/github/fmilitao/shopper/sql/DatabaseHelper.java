package io.github.fmilitao.shopper.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {

    final static String TAG = DatabaseHelper.class.toString();

    DatabaseHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, DBContract.CREATE_SHOPS);
        Log.v(TAG, DBContract.CREATE_ITEMS);

        db.execSQL(DBContract.CREATE_SHOPS);
        db.execSQL(DBContract.CREATE_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "Upgrading from " + oldVersion + " to " + newVersion + ", all destroyed.");

        // Useful stuff on altering existing tables: http://www.w3schools.com/sql/sql_alter.asp
        if( oldVersion < DBContract.DATABASE_VERSION) {
            db.execSQL("ALTER TABLE " + DBContract.ItemEntry.TABLE_NAME + " ADD " + DBContract.ItemEntry.COLUMN_ITEM_UNIT + " TEXT");
            db.execSQL("ALTER TABLE " + DBContract.ItemEntry.TABLE_NAME + " ADD " + DBContract.ItemEntry.COLUMN_ITEM_CATEGORY + " TEXT");
        }

//        else{
////                    db.execSQL("DROP TABLE IF EXISTS " + DBContract.ShopEntry.TABLE_NAME);
////                    db.execSQL("DROP TABLE IF EXISTS " + DBContract.ItemEntry.TABLE_NAME);
////                    onCreate(db);
//                }
//        }

    }
}
