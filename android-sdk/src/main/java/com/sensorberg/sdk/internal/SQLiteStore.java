package com.sensorberg.sdk.internal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.sensorberg.sdk.resolver.BeaconEvent;

import java.util.ArrayList;

public class SQLiteStore {
    private final SQLiteDatabase diskRegistry;

    public SQLiteStore(String fileName, Context context) {
        this.diskRegistry = new RegistryOpenHelper(context, fileName).getWritableDatabase();
    }

    public void put(Entry entry) {
        ContentValues values = new ContentValues(4);
        values.put("_index", entry.index);
        values.put("_timestamp", entry.timestamp);
        values.put("_identifier", entry.identifier);
        values.put("_bundleBlob", ParcelableUtil.marshall(entry.bundle));
        diskRegistry.insertOrThrow("Entry", null, values);
    }

    public int size() {
        Cursor countCursor = diskRegistry.rawQuery("SELECT COUNT(*) FROM Entry", null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();
        return count;
    }

    public ArrayList<Entry> loadRegistry() {
        ArrayList<Entry> values = new ArrayList<>();
        Cursor cursor = diskRegistry.query("Entry", null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                int index = cursor.getInt(0);
                long timestamp = cursor.getLong(1);
                int identifier = cursor.getInt(2);
                Bundle something = ParcelableUtil.unmarshall(cursor.getBlob(3), Bundle.CREATOR);
                something.setClassLoader(BeaconEvent.class.getClassLoader());
                values.add(new Entry(index, timestamp, identifier, something));

            }
            while(cursor.moveToNext());
        }
        cursor.close();

        return values;
    }

    public synchronized void delete(int index) {
        diskRegistry.execSQL("DELETE FROM Entry WHERE _index = " + index);
    }

    public synchronized void deleteByIdentifier(int identifiert) {
        diskRegistry.execSQL("DELETE FROM Entry WHERE _identifier = " + identifiert);
    }

    public void deleteOlderThan(long timestamp) {
        diskRegistry.execSQL("DELETE FROM Entry WHERE _timestamp < " + timestamp);
    }

    public void clear() {
        diskRegistry.execSQL("DELETE FROM Entry");
    }

    public static class Entry {
        public final int index;
        public final long timestamp;
        private final int identifier;
        public final Bundle bundle;

        public Entry(int index, long timestamp, int identifier, Bundle bundle) {
            this.index = index;
            this.timestamp = timestamp;
            this.identifier = identifier;
            this.bundle = bundle;
        }
    }
    class RegistryOpenHelper extends SQLiteOpenHelper
    {
        private static final int VERSION = 2;

        public RegistryOpenHelper(Context context, String name)
        {
            super(context, name, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database)
        {
            database.execSQL("CREATE TABLE Entry (_index INTEGER, _timestamp INTEGER, _identifier INTEGER, _bundleBlob BLOB)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
        {
            database.execSQL("DROP TABLE IF EXISTS Entry");
            onCreate(database);
        }
    }

    public static class ParcelableUtil {

        public static byte[] marshall(Parcelable parceable) {
            Parcel parcel = Parcel.obtain();
            parceable.writeToParcel(parcel, 0);
            byte[] bytes = parcel.marshall();
            parcel.recycle();
            return bytes;
        }

        public static Parcel unmarshall(byte[] bytes) {
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);
            return parcel;
        }

        public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
            Parcel parcel = unmarshall(bytes);
            return creator.createFromParcel(parcel);
        }
    }
}
