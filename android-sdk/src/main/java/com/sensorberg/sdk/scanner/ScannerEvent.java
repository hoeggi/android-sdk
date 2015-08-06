package com.sensorberg.sdk.scanner;

class ScannerEvent {
    final int type;
    final Object data;

    public static final int LOGICAL_SCAN_START_REQUESTED    = 1;
    public static final int SCAN_STOP_REQUESTED             = 2;
    public static final int EVENT_DETECTED                  = 3;
    public static final int PAUSE_SCAN                      = 4;
    public static final int UN_PAUSE_SCAN                   = 5;
    public static final int RSSI_UPDATED                    = 6;

    ScannerEvent(int type, Object data) {
        this.type = type;
        this.data = data;
    }
}