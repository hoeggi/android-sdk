package com.sensorberg.sdk.internal.transport.model;

import com.google.gson.annotations.Expose;

import lombok.Getter;

public class SettingsResponse {

    @Expose
    @Getter
    private int revision;

    @Expose
    @Getter
    private com.sensorberg.sdk.settings.Settings settings;

}
