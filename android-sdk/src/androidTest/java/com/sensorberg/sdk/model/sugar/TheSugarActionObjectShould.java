package com.sensorberg.sdk.model.sugar;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.orm.SugarContext;
import com.orm.SugarDb;
import com.orm.SugarRecord;
import com.orm.query.Select;
import com.orm.util.SugarConfig;
import com.orm.SugarApp;
import com.orm.SugarDb;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testUtils.NoClock;

import org.fest.assertions.api.Assertions;

import java.util.List;
import java.util.UUID;
import static com.orm.SugarRecord.save;
import io.realm.RealmResults;
import util.TestConstants;

/**
 * Created by skraynick on 16-03-15.
 */
public class TheSugarActionObjectShould extends AndroidTestCase {

    private SugarAction tested;
    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");
    private Clock clock;
    private SugarDb db;


    @Override
    public void setUp() throws Exception {
        //SugarRecord.getEntitiesFromCursor()
        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(new InAppAction(uuid, null, null, null, null, 0))
                .withPresentationTime(1337)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .build();
        beaconEvent.setBeaconId(TestConstants.ANY_BEACON_ID);
        clock = NoClock.CLOCK;
        tested = SugarAction.from(beaconEvent, clock);
        //tested.save();

    }

    public void test_tested_object_should_not_be_null() {
        Assertions.assertThat(tested).isNotNull();
        //Assertions.assertThat(tested)....
    }

    public void test_should_be_json_serializeable() throws Exception {

        String objectAsJSON = HeadersJsonObjectRequest.gson.toJson(tested);

        Assertions.assertThat(objectAsJSON)
                .isNotEmpty()
                .isEqualToIgnoringCase("{\"actionId\":\"6133172d-935f-437f-b932-a901265c24b0\",\"createdAt\":0,\"keepForever\":false,\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"sentToServerTimestamp\":0,\"sentToServerTimestamp2\":-9223372036854775808,\"timeOfPresentation\":1337,\"trigger\":1}");
    }

    public void test_should_serialize_a_list_of_objects() throws Exception {

        //List<SugarAction> objects = SugarRecord.find(SugarAction.class, "*");

                //Select.from(SugarAction.class).list();

       // String objectsAsJson = HeadersJsonObjectRequest.gson.toJson(objects);

        /*Assertions.assertThat(objectsAsJson)
                .isNotEmpty()
                .isEqualToIgnoringCase("[{\"eid\":\"6133172D-935F-437F-B932-A901265C24B0\",\"trigger\":1,\"pid\":\"192E463C9B8E4590A23FD32007299EF50133701337\",\"dt\":\"1970-01-01T01:00:01.337+01:00\"}]");*/
    }
}
