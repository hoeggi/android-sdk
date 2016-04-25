package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.DefaultSettings;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import util.TestConstants;

import static com.sensorberg.sdk.scanner.RecordedRequestAssert.assertThat;

public class TheBeaconActionHistoryPublisherIntegrationShould extends SensorbergApplicationTest {

    @Inject
    @Named("realHandlerManager")
    HandlerManager testHandleManager;

    @Inject
    @Named("realClock")
    Clock clock;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    @Named("realTransport")
    Transport transport;

    private ScanEvent SCAN_EVENT;

    private BeaconActionHistoryPublisher tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = new BeaconActionHistoryPublisher(transport, ResolverListener.NONE, DefaultSettings.DEFAULT_CACHE_TTL, clock, testHandleManager);

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

        RecordedRequest request = server.takeRequest();

        assertThat(request).matchesRawResourceRequest(com.sensorberg.sdk.test.R.raw.request_reporting_001, getContext());
    }
}
