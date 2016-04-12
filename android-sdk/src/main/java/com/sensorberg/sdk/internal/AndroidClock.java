package com.sensorberg.sdk.internal;

import android.os.SystemClock;

public class AndroidClock implements Clock {
    @Override
    public long now() {
        return System.currentTimeMillis();
    }

    @Override
    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }
}
