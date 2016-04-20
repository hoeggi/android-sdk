package com.sensorberg.sdk.model.sugar;

import android.test.AndroidTestCase;
import com.sensorbergorm.query.Select;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import org.fest.assertions.api.Assertions;

import util.TestConstants;

public class TheSugarHistoryBodyShould extends AndroidTestCase {

    private HistoryBody tested;
    private SugarScan scans;

    @Override
    public void setUp() throws Exception {
        super.setUp();

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
        String asJSONStrion = HeadersJsonObjectRequest.gson.toJson(tested);

        Assertions.assertThat(asJSONStrion).isNotEmpty();
    }
}
