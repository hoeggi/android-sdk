package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.settings.Settings;

import android.os.Bundle;

public class Scanner extends AbstractScanner {
    private static final String SCANNER_EVENT = "com.sensorberg.sdk.scanner.SDKScanner.SCANNER_EVENT";

    public Scanner(Settings settings, Platform platform, boolean shouldRestoreBeaconStates, Clock clock, FileManager fileManager,
            ServiceScheduler scheduler, HandlerManager handlerManager) {
        super(settings, platform, shouldRestoreBeaconStates, clock, fileManager, scheduler, handlerManager);
    }

    @Override
    protected void clearScheduledExecutions() {
        serviceScheduler.cancelServiceMessage(indexFor(ScannerEvent.PAUSE_SCAN));
        serviceScheduler.cancelServiceMessage(indexFor(ScannerEvent.UN_PAUSE_SCAN));
    }

    @Override
    protected void scheduleExecution(int type, long delay) {
        Bundle bundle = new Bundle();
        bundle.putInt(Scanner.SCANNER_EVENT, type);
        serviceScheduler.postToServiceDelayed(delay, SensorbergService.MSG_SDK_SCANNER_MESSAGE, bundle, false, indexFor(type));
    }

    private int indexFor(int type) {
        return -1000 - type;
    }

    public void handlePlatformMessage(Bundle what){
        int messageId = what.getInt(SCANNER_EVENT, -1);
        if (messageId == ScannerEvent.UN_PAUSE_SCAN){
            runLoop.sendMessage(ScannerEvent.UN_PAUSE_SCAN);
        } else if(messageId == ScannerEvent.PAUSE_SCAN) {
            runLoop.sendMessage(ScannerEvent.PAUSE_SCAN);
        } else{
            Logger.log.logError("unknown scheduled execution:" + messageId);
        }
    }
}
