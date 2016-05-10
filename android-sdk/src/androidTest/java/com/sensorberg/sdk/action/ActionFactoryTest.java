package com.sensorberg.sdk.action;

import com.google.gson.JsonObject;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.IOException;

import util.Utils;

@RunWith(AndroidJUnit4.class)
public class ActionFactoryTest {

    private JsonObject URI_JSON_OBJECT;

    private static final int[] payloadSamples = new int[]{
            com.sensorberg.sdk.test.R.raw.action_factory_payload_001_array,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_002_object,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_004_integer,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_003_boolean,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_005_string,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_006_double,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_008_integer_with_exponent,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_009_empty_string,
    };

    @Before
    public void setUp() throws Exception {
        URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_001, InstrumentationRegistry.getContext());
    }

    @Test
    public void should_parse_server_output() {
        try {
            UriMessageAction result = (UriMessageAction) ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getContent()).isEqualTo("This is a message");
            Assertions.assertThat(result.getTitle()).isEqualTo("this is a subject");
            Assertions.assertThat(result.getUri()).isEqualTo("something://");
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_all_non_null_values() throws Exception {
        for (int i : payloadSamples) {
            Action action = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(i, InstrumentationRegistry.getContext()));
            Assertions.assertThat(action.getPayload()).isNotNull();
        }
    }

    @Test
    public void should_parse_null_payloads() throws IOException, JSONException {
        Action action = ActionFactory.actionFromJSONObject(
                Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_007_null, InstrumentationRegistry.getContext()));
        Assertions.assertThat(action.getPayload()).isNull();
    }
}