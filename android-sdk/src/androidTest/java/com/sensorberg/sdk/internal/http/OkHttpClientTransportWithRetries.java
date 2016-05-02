package com.sensorberg.sdk.internal.http;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.settings.Settings;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

public class OkHttpClientTransportWithRetries extends SensorbergApplicationTest {

    @Inject
    @Named("noClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    @Inject
    @Named("realRetrofitApiService")
    RetrofitApiServiceImpl realRetrofitApiService;

    protected Transport tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        //TODO

//        BasicNetwork network = new BasicNetwork(new OkHttpStackWithFailures(2));
//
//        File cacheDir = new File(getContext().getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));
//        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
//        queue.start();

        tested = new RetrofitApiTransport(realRetrofitApiService, clock);
        tested.setApiToken(TestConstants.API_TOKEN);
    }

    public void test_succeed_even_after_two_failures() throws Exception {
        tested.loadSettings(new TransportSettingsCallback() {
            @Override
            public void nothingChanged() {
                fail();
            }

            @Override
            public void onFailure(Exception e) {
                fail();
            }

            @Override
            public void onSettingsFound(Settings settings) {

            }
        });

    }
}
