package com.sensorberg.sdk.internal;

import android.content.Context;
import android.os.Bundle;

import com.sensorberg.SensorbergApplication;

import java.util.ArrayList;

import javax.inject.Inject;

public class PendingIntentStorage {
    private final Platform platform;
    private final SQLiteStore storage;

    @Inject
    Context context;

    @Inject
    Clock clock;

    public PendingIntentStorage(Platform platform) {
        this.platform = platform;
        SensorbergApplication.getComponent().inject(this);
        storage = new SQLiteStore("pendingIntentStorage.sqlite", context);
    }

    public void add(int index, long timestamp, int identifier, Bundle bundle) {
        storage.deleteByIdentifier(identifier);
        storage.put(new SQLiteStore.Entry(index, timestamp, identifier, bundle));
    }

    public void restorePendingIntents() {
        storage.deleteOlderThan(clock.now());
        ArrayList<SQLiteStore.Entry> entries = storage.loadRegistry();
        for (SQLiteStore.Entry entry : entries) {
            long relativeFromNow = entry.timestamp - clock.now();
            platform.scheduleIntent(entry.index, relativeFromNow, entry.bundle);
        }
    }

    public void clearAllPendingIntents() {
        ArrayList<SQLiteStore.Entry> entries = storage.loadRegistry();
        for (SQLiteStore.Entry entry : entries) {
            platform.unscheduleIntent(entry.index);
        }
        storage.clear();
    }
    public void removeStoredPendingIntent(int index){
        storage.delete(index);
    }
}
