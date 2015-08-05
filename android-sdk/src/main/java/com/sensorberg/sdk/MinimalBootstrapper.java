package com.sensorberg.sdk;

import com.sensorberg.sdk.internal.Platform;

public class MinimalBootstrapper {

    protected final Platform platform;

    public MinimalBootstrapper(Platform platform) {
        this.platform = platform;
    }
    public void unscheduleAllPendingActions() {
        platform.clearAllPendingIntents();
    }

    public void stopAllScheduledOperations() {
        platform.cancelAllScheduledTimer();
    }

    public void stopScanning() {
        //we don´ care, we´e not scanning
    }
}
