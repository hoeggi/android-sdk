package com.sensorberg.sdk.model.sugar;

import android.test.AndroidTestCase;

import com.sensorbergorm.SugarRecord;
import com.sensorbergorm.query.Select;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testUtils.NoClock;
import org.fest.assertions.api.Assertions;
import java.util.List;
import util.TestConstants;

public class TheSugarScanobjectShould extends AndroidTestCase {

    private SugarScan tested;
    private Clock clock;
    private List<SugarScan> objects;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        SugarScan.deleteAll(SugarScan.class);
        ScanEvent scanevent = new ScanEvent.Builder()
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEventTime(100)
                .build();
        clock = NoClock.CLOCK;
        tested = SugarScan.from(scanevent, 0);
    }

    public void test_should_generate_a_bid() throws Exception {
        Assertions.assertThat(tested.getPid()).isEqualToIgnoringCase("192E463C9B8E4590A23FD32007299EF50133701337");
    }

    public void test_should_be_json_serializeable() throws Exception {
        String objectAsJSON = HeadersJsonObjectRequest.gson.toJson(tested);

       Assertions.assertThat(objectAsJSON)
                .isNotEmpty()
                .isEqualToIgnoringCase("{\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"trigger\":1,\"dt\":\"1970-01-01T01:00:00.100+01:00\"}");
    }

    public void test_should_serialize_a_list_of_objects() throws Exception {
        tested.save();
        List<SugarScan> objects = SugarRecord.find(SugarScan.class, "");
        Select.from(SugarScan.class).list();
        String objectsAsJson = HeadersJsonObjectRequest.gson.toJson(objects);

        Assertions.assertThat(objectsAsJson)
               .isNotEmpty()
                .isEqualToIgnoringCase("[{\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"trigger\":1,\"dt\":\"1970-01-01T01:00:00.100+01:00\"}]");
    }
}
