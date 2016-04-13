package com.sensorberg.sdk.scanner;

import android.util.Log;

import com.sensorbergorm.SugarContext;
import com.sensorberg.sdk.SensorbergApplicationTest;
import util.TestConstants;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.internal.transport.HistoryCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.testUtils.TestPlatform;
import java.util.List;
import java.util.UUID;
import static util.Verfier.hasSize;
import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TheBeaconActionHistoryPublisherShould extends SensorbergApplicationTest {
    private BeaconActionHistoryPublisher tested;

    private Transport transport;
    private TestPlatform testPlattform;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        SugarAction.deleteAll(SugarAction.class);
        SugarScan.deleteAll(SugarScan.class);
        testPlattform = new TestPlatform().setContext(getContext());
        testPlattform.clock.setNowInMillis(System.currentTimeMillis());

        transport = mock(Transport.class);
        testPlattform.setTransport(transport);
        tested = new BeaconActionHistoryPublisher(testPlattform, ResolverListener.NONE, null);

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
        for (SugarScan thing: notSentObjects) {
            Log.i("things", Long.toString(thing.getId()));
        }
        assertThat(notSentObjects).hasSize(1);

    }

    public void test_should_persist_actions_that_need_queing() throws Exception {
        //SugarAction.deleteAll(SugarAction.class);
        List<SugarAction> notSentObjects = SugarAction.notSentScans();
        assertThat(notSentObjects).hasSize(1);
    }

    public void test_should_schedule_the_sending_of_one_the_unsent_objects() throws Exception {
        tested.publishHistory();
        verify(transport).publishHistory(hasSize(1), hasSize(1), any(HistoryCallback.class));
    }
}
