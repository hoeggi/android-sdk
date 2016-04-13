package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.model.realm.RealmScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestPlatform;

import java.util.UUID;

import util.TestConstants;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class TheBeconHistorySynchronousIntegrationTest extends SensorbergApplicationTest {
    private BeaconActionHistoryPublisher tested;

    private Transport transport;
    private TestPlatform testPlattform;


    @Override
    public void setUp() throws Exception {
        super.setUp();

        testPlattform = new TestPlatform();
        testPlattform.clock.setNowInMillis(System.currentTimeMillis());
        transport = spy(new DumbSucessTransport());
        testPlattform.setTransport(transport);
        Settings settings = mock(Settings.class);
        tested = new BeaconActionHistoryPublisher(testPlattform, transport, ResolverListener.NONE, settings, testPlattform.clock);

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
        assertThat(RealmScan.notSentScans(getRealmInstance())).hasSize(0);
    }
}
