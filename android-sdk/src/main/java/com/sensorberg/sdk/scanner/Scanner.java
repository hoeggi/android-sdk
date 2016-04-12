package com.sensorberg.sdk.scanner;

import android.os.Bundle;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.settings.Settings;

public class Scanner extends AbstractScanner {
    private static final String SCANNER_EVENT = "com.sensorberg.sdk.scanner.SDKScanner.SCANNER_EVENT";

    public Scanner(Settings settings, Platform platform, boolean shouldRestoreBeaconStates, Clock clock) {
        super(settings, platform, shouldRestoreBeaconStates, clock);
        SensorbergApplication.getComponent().inject(this);
    }

    @Override
    protected void clearScheduledExecutions() {
        platform.cancelServiceMessage(indexFor(ScannerEvent.PAUSE_SCAN));
        platform.cancelServiceMessage(indexFor(ScannerEvent.UN_PAUSE_SCAN));
    }

    @Override
    protected void scheduleExecution(int type, long delay) {
        Bundle bundle = new Bundle();
        bundle.putInt(Scanner.SCANNER_EVENT, type);
        platform.postToServiceDelayed(delay, SensorbergService.MSG_SDK_SCANNER_MESSAGE, bundle, false, indexFor(type));
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
