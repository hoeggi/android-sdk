package util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author skraynick
 * @date 16-04-13
 */
public class TestDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 2;

    static final String DATABASE_CREATE_SCANS  = "create table SUGAR_SCAN" + "( " + "ID integer primary key autoincrement, CREATED_AT long, EVENT_TIME text, " +
            "IS_ENTRY boolean, PROXIMITY_MAJOR integer, PROXIMITY_MINOR integer, PROXIMITY_UUID integer, SENT_TO_SERVER_TIMESTAMP long, SENT_TO_SERVER_TIMESTAMP2 long); ";

    static final String DATABASE_CREATE_ACTIONS  = "create table SUGAR_ACTION" + "( " + "ID integer primary key autoincrement, ACTION_ID integer, CREATED_AT long, " +
            "KEEP_FOREVER boolean, PID text, SENT_TO_SERVER_TIMESTAMP long, SENT_TO_SERVER_TIMESTAMP2 long, TIME_OF_PRESENTATION long, TRIGGER integer); ";

    public TestDatabaseHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_ACTIONS);
        db.execSQL(DATABASE_CREATE_SCANS);
    }

    // Called when there is a database version mismatch meaning that the version
    // of the database on disk needs to be upgraded to the current version.
    @Override
    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
        // Log the version upgrade.
        Log.w("TaskDBAdapter", "Upgrading from version " + _oldVersion + " to " + _newVersion + ", which will destroy all old data");

        // Upgrade the existing database to conform to the new version. Multiple
        // previous versions can be handled by comparing _oldVersion and
        // _newVersion
        // values.
        // The simplest case is to drop the old table and create a new one.
        _db.execSQL("DROP TABLE IF EXISTS " + "TEMPLATE");
        // Create a new one.
        onCreate(_db);
    }
}
