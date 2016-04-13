package com.sensorberg.sdk.model.realm;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import org.fest.assertions.api.Assertions;

import android.test.AndroidTestCase;

import java.util.Collections;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import io.realm.Realm;
import io.realm.RealmResults;
import util.TestConstants;

public class TheRealmActionObjectShould extends AndroidTestCase {

    private RealmAction tested;
    private Realm realm;
    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");

    @Inject
    @Named("noClock")
    Clock clock;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        realm = Realm.getInstance(getContext(), "test" + System.currentTimeMillis());

        realm.beginTransaction();
        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(new InAppAction(uuid, null, null, null, null, 0))
                .withPresentationTime(1337)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .build();
        beaconEvent.setBeaconId(TestConstants.ANY_BEACON_ID);
        tested = RealmAction.from(beaconEvent, realm, clock);
        realm.commitTransaction();
    }


    public void test_should_be_json_serializeable() throws Exception {

        String objectAsJSON = HeadersJsonObjectRequest.gson.toJson(tested);

        Assertions.assertThat(objectAsJSON)
                .isNotEmpty()
                .isEqualToIgnoringCase("{\"eid\":\"6133172D-935F-437F-B932-A901265C24B0\",\"trigger\":1,\"pid\":\"192E463C9B8E4590A23FD32007299EF50133701337\",\"dt\":\"1970-01-01T01:00:01.337+01:00\"}");
    }

    public void test_should_serialize_a_list_of_objects() throws Exception {
        RealmResults<?> objects = realm.allObjects(RealmAction.class);

        String objectsAsJson = HeadersJsonObjectRequest.gson.toJson(objects);

        Assertions.assertThat(objectsAsJson)
                .isNotEmpty()
                .isEqualToIgnoringCase("[{\"eid\":\"6133172D-935F-437F-B932-A901265C24B0\",\"trigger\":1,\"pid\":\"192E463C9B8E4590A23FD32007299EF50133701337\",\"dt\":\"1970-01-01T01:00:01.337+01:00\"}]");
    }

    public void test_should_remove_all_objects_older_that_one_Month() throws Exception {

        RealmResults<RealmAction> allActions = realm.allObjects(RealmAction.class);
        Assertions.assertThat(allActions).hasSize(1);

        RealmAction.markAsSent(allActions, realm, clock.now(), Constants.Time.ONE_DAY * 30);
        RealmAction.markAsSent(Collections.<RealmAction>emptyList(), realm, Constants.Time.ONE_DAY * 30 + 1, Constants.Time.ONE_DAY * 30);

        RealmResults<RealmAction> allActionsAfterDeletion = realm.allObjects(RealmAction.class);
        Assertions.assertThat(allActionsAfterDeletion).hasSize(0);

    }
}
