package com.sensorberg.sdk.model.realm;

import android.test.AndroidTestCase;

import util.TestConstants;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import org.fest.assertions.api.Assertions;

import io.realm.Realm;

public class TheHistoryBodyShould extends AndroidTestCase {

    private HistoryBody tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Realm realm = Realm.getInstance(getContext(), "test" + System.currentTimeMillis());

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

        realm.beginTransaction();
        RealmScan.from(scanevent, realm, clock.now());
        realm.commitTransaction();



        tested = new HistoryBody(realm.allObjects(RealmScan.class), null, clock);
    }

    public void test_should_be_serializeable() throws Exception {
        String asJSONStrion = HeadersJsonObjectRequest.gson.toJson(tested);

        Assertions.assertThat(asJSONStrion).isNotEmpty();

    }
}
