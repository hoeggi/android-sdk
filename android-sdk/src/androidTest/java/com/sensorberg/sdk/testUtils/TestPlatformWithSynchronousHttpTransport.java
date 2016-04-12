package com.sensorberg.sdk.testUtils;

import android.content.Context;

import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.Transport;

import util.TestConstants;

public class TestPlatformWithSynchronousHttpTransport extends TestPlatform {
    private final Context context;
    private final Transport transport;


    public TestPlatformWithSynchronousHttpTransport(Context context) {
        super();
        this.context = context;
        transport = new OkHttpClientTransport(this, null, getCachedVolleyQueue(), clock);
        transport.setApiToken(TestConstants.API_TOKEN);
    }

    @Override
    public Transport getTransport() {
        return transport;
    }

    @Override
    public boolean isSyncEnabled() {
        return false;
    }
}
