package com.sensorberg.sdk.internal.interfaces;

public interface SettingsChangedListener {

    SettingsChangedListener NONE = new SettingsChangedListener() {
        @Override
        public void historyUploadIntervalChanged(Long newHistoryUploadIntervalMillis) {
            //do nothing
        }
    };

    void historyUploadIntervalChanged(Long newHistoryUploadIntervalMillis);

}
