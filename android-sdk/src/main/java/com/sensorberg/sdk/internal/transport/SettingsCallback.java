package com.sensorberg.sdk.internal.transport;

import com.android.sensorbergVolley.VolleyError;

import org.json.JSONObject;

public interface SettingsCallback {
    SettingsCallback NONE = new SettingsCallback() {
        @Override
        public void nothingChanged() {

        }

        @Override
        public void onFailure(VolleyError e) {

        }

        @Override
        public void onSettingsFound(JSONObject settings) {

        }
    };

    void nothingChanged();

    void onFailure(VolleyError e);

    void onSettingsFound(JSONObject settings);
}
