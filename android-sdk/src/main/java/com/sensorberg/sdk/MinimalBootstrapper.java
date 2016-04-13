package com.sensorberg.sdk;

import com.sensorberg.sdk.internal.interfaces.Platform;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;

class MinimalBootstrapper {

    final ServiceScheduler serviceScheduler;

    final Platform platform;

    public MinimalBootstrapper(Platform platform, ServiceScheduler scheduler) {
        this.platform = platform;
        serviceScheduler = scheduler;
    }

    public void unscheduleAllPendingActions() {
        serviceScheduler.clearAllPendingIntents();
    }

    public void stopAllScheduledOperations() {
        serviceScheduler.cancelAllScheduledTimer();
    }

    public void stopScanning() {
        //we don´ care, we´e not scanning
    }
}
