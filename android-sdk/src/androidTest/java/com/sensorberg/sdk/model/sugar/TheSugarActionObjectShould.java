package com.sensorberg.sdk.model.sugar;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testUtils.NoClock;
import com.sensorbergorm.SugarRecord;
import com.sensorbergorm.query.Select;

import org.fest.assertions.api.Assertions;

import java.util.List;
import java.util.UUID;

import util.TestConstants;

/**
 * Created by skraynick on 16-03-15.
 */
public class TheSugarActionObjectShould extends SensorbergApplicationTest {

    private SugarAction tested;
    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");
    private Clock clock;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        SugarAction.deleteAll(SugarAction.class);
        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(new InAppAction(uuid, null, null, null, null, 0))
                .withPresentationTime(1337)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .build();
        beaconEvent.setBeaconId(TestConstants.ANY_BEACON_ID);
        clock = NoClock.CLOCK;
        tested = SugarAction.from(beaconEvent, clock);
    }

    public void test_tested_object_should_not_be_null() {
        Assertions.assertThat(tested).isNotNull();
    }

    public void test_should_be_json_serializeable() throws Exception {
        String objectAsJSON = HeadersJsonObjectRequest.gson.toJson(tested);

        Assertions.assertThat(objectAsJSON)
                .isNotEmpty()
                .isEqualToIgnoringCase("{\"eid\":\"6133172d-935f-437f-b932-a901265c24b0\",\"trigger\":1,\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"dt\":\"1970-01-01T01:00:01.337+01:00\"}");
    }

    public void test_should_serialize_a_list_of_objects() throws Exception {
        tested.save();

        List<SugarAction> objects = SugarRecord.find(SugarAction.class, "");
        Select.from(SugarAction.class).list();

        String objectsAsJson = HeadersJsonObjectRequest.gson.toJson(objects);

        Assertions.assertThat(objectsAsJson)
                .isNotEmpty()
                .isEqualToIgnoringCase("[{\"eid\":\"6133172d-935f-437f-b932-a901265c24b0\",\"trigger\":1,\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"dt\":\"1970-01-01T01:00:01.337+01:00\"}]");
        SugarAction.deleteAll(SugarAction.class);
    }
}
