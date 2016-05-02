package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestHandlerManager;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.fest.assertions.api.Assertions.assertThat;

public class TheBeconHistorySynchronousIntegrationTest extends SensorbergApplicationTest {

    @Inject
    TestHandlerManager testHandlerManager;

    @Inject
    @Named("dummyTransportSettingsManager")
    SettingsManager testSettingsManager;

    private BeaconActionHistoryPublisher tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testHandlerManager.getCustomClock().setNowInMillis(System.currentTimeMillis());
        tested = new BeaconActionHistoryPublisher(getContext(), new DumbSucessTransport(), testSettingsManager, testHandlerManager.getCustomClock(),
                testHandlerManager);

        tested.onScanEventDetected(new ScanEvent.Builder()
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEventTime(100)
                .build());

        tested.onActionPresented(new BeaconEvent.Builder()
                .withAction(new VisitWebsiteAction(UUID.randomUUID(), "foo", "bar", null, null, 0))
                .withPresentationTime(1337)
                .build());
    }

    public void test_should_mark_sent_objects_as_sent() throws Exception {
        tested.publishHistory();
        assertThat(SugarScan.notSentScans()).hasSize(0);
    }
}
