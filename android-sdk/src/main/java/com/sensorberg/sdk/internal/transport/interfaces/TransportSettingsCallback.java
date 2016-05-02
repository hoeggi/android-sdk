package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.settings.Settings;

public interface TransportSettingsCallback {
    TransportSettingsCallback NONE = new TransportSettingsCallback() {
        @Override
        public void nothingChanged() {

        }

        @Override
        public void onFailure(Exception e) {

        }

        @Override
        public void onSettingsFound(Settings settings) {

        }
    };

    void nothingChanged();

    void onFailure(Exception e);

    void onSettingsFound(Settings settings);
}
