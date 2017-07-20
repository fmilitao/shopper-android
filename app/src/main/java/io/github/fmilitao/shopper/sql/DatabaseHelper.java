package io.github.fmilitao.shopper.sql;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getCanonicalName();

    SQLiteDatabase mDb;

    DatabaseHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
    }

    public void open() throws SQLException {
        if (mDb == null) {
            // always uses the same writable database, even when reading
            // TODO: move 'getWritableDatabase' off the main thread. Maybe use AsyncTask?
            mDb = getWritableDatabase();
        }
    }

    @Override
    public void close() {
        super.close();
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            // Enable foreign key constraints since these are not enabled by default.
            // This PRAGMA must be set before the connection is used.
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.CREATE_SHOPS);
        db.execSQL(DBContract.CREATE_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "Upgrading from " + oldVersion + " to " + newVersion + ".");

        if (oldVersion < 10) {
            // DB version is too old: hard update by dropping all and create new
            // Not exactly user friendly since it will erase all old content...
            Log.w(TAG, "Version is too old. Will erase everything and create new tables.");

            // TODO should export all to a csv file! ... and then try to re-import?
            db.execSQL("DROP TABLE IF EXISTS " + DBContract.ShopEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DBContract.ItemEntry.TABLE_NAME);
            onCreate(db);
        } else {
            // Will upgrade the tables by adding the missing columns.
            // This should work as long as the new columns are nullable.
            // Reference on altering tables: http://www.w3schools.com/sql/sql_alter.asp

            if (oldVersion < 12) {
                // add units column
                db.execSQL(
                        "ALTER TABLE " + DBContract.ItemEntry.TABLE_NAME +
                                " ADD " + DBContract.ItemEntry.COLUMN_ITEM_UNIT + " TEXT"
                );
            }

            if (oldVersion < 13) {
                // add categories column
                db.execSQL("ALTER TABLE " + DBContract.ItemEntry.TABLE_NAME +
                        " ADD " + DBContract.ItemEntry.COLUMN_ITEM_CATEGORY + " TEXT");
            }
        }
    }
}
