package com.sensorberg.sdk.internal;

import android.os.Bundle;

import java.util.ArrayList;

class PendingIntentStorage {
    private final Platform platform;
    private final SQLiteStore storage;

    public PendingIntentStorage(Platform platform) {
        this.platform = platform;
        storage = new SQLiteStore("pendingIntentStorage.sqlite", platform.getContext());
    }

    public void add(int index, long timestamp, int identifier, Bundle bundle) {
        storage.deleteByIdentifier(identifier);
        storage.put(new SQLiteStore.Entry(index, timestamp, identifier, bundle));
    }

    public void restorePendingIntents() {
        storage.deleteOlderThan(platform.getClock().now());
        ArrayList<SQLiteStore.Entry> entries = storage.loadRegistry();
        for (SQLiteStore.Entry entry : entries) {
            long relativeFromNow = entry.timestamp - platform.getClock().now();
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
