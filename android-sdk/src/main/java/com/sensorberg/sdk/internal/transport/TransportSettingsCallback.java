package com.sensorberg.sdk.internal.transport;

import org.json.JSONObject;

public interface TransportSettingsCallback {
    TransportSettingsCallback NONE = new TransportSettingsCallback() {
        @Override
        public void nothingChanged() {

        }

        @Override
        public void onFailure(Exception e) {

        }

        @Override
        public void onSettingsFound(JSONObject settings) {

        }
    };

    void nothingChanged();

    void onFailure(Exception e);

    void onSettingsFound(JSONObject settings);
}
