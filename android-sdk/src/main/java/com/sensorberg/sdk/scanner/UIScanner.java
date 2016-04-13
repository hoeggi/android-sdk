package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.settings.Settings;

public class UIScanner extends AbstractScanner {

    public UIScanner(Settings settings, Platform platform, Clock clock, FileManager fileManager, ServiceScheduler scheduler,
            HandlerManager handlerManager) {
        super(settings, platform, false, clock, fileManager, scheduler, handlerManager);
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
