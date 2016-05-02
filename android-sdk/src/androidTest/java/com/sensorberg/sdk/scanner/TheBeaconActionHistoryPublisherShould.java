package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.TransportHistoryCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestHandlerManager;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static util.Verfier.hasSize;

public class TheBeaconActionHistoryPublisherShould extends SensorbergApplicationTest {

    @Inject
    TestHandlerManager testHandlerManager;

    private BeaconActionHistoryPublisher tested;

    private Transport transport;

    @Inject
    @Named("dummyTransportSettingsManager")
    SettingsManager testSettingsManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testHandlerManager.getCustomClock().setNowInMillis(System.currentTimeMillis());
        SugarAction.deleteAll(SugarAction.class);
        SugarScan.deleteAll(SugarScan.class);
        transport = mock(Transport.class);
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

    public void test_should_persist_scans_that_need_queing() throws Exception {
        List<SugarScan> notSentObjects = SugarScan.notSentScans();
        assertThat(notSentObjects).hasSize(1);
    }

    public void test_should_persist_actions_that_need_queing() throws Exception {
        //SugarAction.deleteAll(SugarAction.class);
        List<SugarAction> notSentObjects = SugarAction.notSentScans();
        assertThat(notSentObjects).hasSize(1);
    }

    public void test_should_schedule_the_sending_of_one_the_unsent_objects() throws Exception {
        tested.publishHistory();
        verify(transport).publishHistory(hasSize(1), hasSize(1), any(TransportHistoryCallback.class));
    }
}
