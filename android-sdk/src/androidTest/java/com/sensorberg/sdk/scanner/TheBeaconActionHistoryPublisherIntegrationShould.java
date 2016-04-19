package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.DefaultSettings;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.squareup.okhttp.mockwebserver.MockResponse;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

public class TheBeaconActionHistoryPublisherIntegrationShould extends SensorbergApplicationTest {

    @Inject
    @Named("realHandlerManager")
    HandlerManager testHandleManager;

    @Inject
    @Named("noClock")
    Clock clock;

    @Inject
    SharedPreferences sharedPreferences;

    private ScanEvent SCAN_EVENT;

    private BeaconActionHistoryPublisher tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = new BeaconActionHistoryPublisher(new DumbSucessTransport(), ResolverListener.NONE, DefaultSettings.DEFAULT_CACHE_TTL, clock, testHandleManager);

        startWebserver();
        server.enqueue(new MockResponse().setBody("{}"));
        SCAN_EVENT = new ScanEvent.Builder()
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEventTime(100)
                .build();
    }

    public void test_should_send_history_to_the_server() throws Exception {
        tested.onScanEventDetected(SCAN_EVENT);
        tested.publishHistory();

        //TODO
        fail();
//        RecordedRequest request = server.takeRequest();

//        assertThat(request).matchesRawResourceRequest(com.sensorberg.sdk.test.R.raw.request_reporting_001, getContext());
    }
}
