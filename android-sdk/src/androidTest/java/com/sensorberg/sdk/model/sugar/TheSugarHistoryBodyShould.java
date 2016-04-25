package com.sensorberg.sdk.model.sugar;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorbergorm.query.Select;

import org.fest.assertions.api.Assertions;

import android.test.AndroidTestCase;

import javax.inject.Inject;

import util.TestConstants;

public class TheSugarHistoryBodyShould extends AndroidTestCase {

    @Inject
    Gson gson;

    private HistoryBody tested;
    private SugarScan scans;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        ScanEvent scanevent = new ScanEvent.Builder()
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEventTime(100)
                .build();
        Clock clock = new Clock() {
            @Override
            public long now() {
                return 1337;
            }

            @Override
            public long elapsedRealtime() {
                return 0;
            }
        };

        scans = SugarScan.from(scanevent, clock.now());
        tested = new HistoryBody(Select.from(SugarScan.class).list(), null, clock);
    }

    public void test_should_be_serializeable() throws Exception {
        String asJSONStrion = gson.toJson(tested);

        Assertions.assertThat(asJSONStrion).isNotEmpty();
    }
}
