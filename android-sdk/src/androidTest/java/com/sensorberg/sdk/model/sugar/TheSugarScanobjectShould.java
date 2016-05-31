package com.sensorberg.sdk.model.sugar;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorbergorm.SugarRecord;
import com.sensorbergorm.query.Select;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import javax.inject.Inject;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TheSugarScanobjectShould {

    @Inject
    Gson gson;

    private SugarScan tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        SugarScan.deleteAll(SugarScan.class);
        ScanEvent scanevent = new ScanEvent.Builder()
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEventTime(100)
                .build();
        tested = SugarScan.from(scanevent, 0);
    }

    @Test
    public void test_should_generate_a_bid() throws Exception {
        Assertions.assertThat(tested.getPid()).isEqualToIgnoringCase("192E463C9B8E4590A23FD32007299EF50133701337");
    }

    @Test
    public void test_should_be_json_serializeable() throws Exception {
        String objectAsJSON = gson.toJson(tested);

        Assertions.assertThat(objectAsJSON)
                .isNotEmpty()
                .isEqualToIgnoringCase(
                        "{\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"trigger\":1,\"dt\":\"1970-01-01T01:00:00.100+01:00\"}");
    }

    @Test
    public void test_should_serialize_a_list_of_objects() throws Exception {
        tested.save();
        List<SugarScan> objects = SugarRecord.find(SugarScan.class, "");
        Select.from(SugarScan.class).list();
        String objectsAsJson = gson.toJson(objects);

        Assertions.assertThat(objectsAsJson)
                .isNotEmpty()
                .isEqualToIgnoringCase(
                        "[{\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"trigger\":1,\"dt\":\"1970-01-01T01:00:00.100+01:00\"}]");
    }
}
