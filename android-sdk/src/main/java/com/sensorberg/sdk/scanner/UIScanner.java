package com.sensorberg.sdk.scanner;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.settings.Settings;

public class UIScanner extends AbstractScanner {

    public UIScanner(Settings settings, Platform platform, Clock clock, FileManager fileManager) {
        super(settings, platform, false, clock, fileManager);
        SensorbergApplication.getComponent().inject(this);
    }

    @Override
    protected void clearScheduledExecutions() {
        runLoop.clearScheduledExecutions();
    }

    @Override
    void scheduleExecution(final int type, long delay) {
        runLoop.scheduleExecution(new Runnable() {
            @Override
            public void run() {
                runLoop.sendMessage(type);
            }
        }, delay);
    }
}
