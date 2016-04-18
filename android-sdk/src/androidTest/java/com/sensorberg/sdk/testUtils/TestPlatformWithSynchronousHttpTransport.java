package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.Transport;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

public class TestPlatformWithSynchronousHttpTransport extends TestPlatform {

    @Inject
    @Named("realClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    private final Context context;
    private final Transport transport;

    public TestPlatformWithSynchronousHttpTransport(Context context) {
        super();
        this.context = context;
        transport = new OkHttpClientTransport(this, null, getCachedVolleyQueue(), clock, testPlatformIdentifier);
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
