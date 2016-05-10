package com.sensorberg.sdk.model.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import util.Utils;

@RunWith(AndroidJUnit4.class)
public class ResolveActionTest {

    @Inject
    Gson gson;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Test
    public void test_should_be_parseable() throws Exception {
        ResolveAction tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_001, InstrumentationRegistry
                .getContext()), ResolveAction.class);

        Assertions.assertThat(tested.content.get("url")).isNotNull();
        Assertions.assertThat(tested.content.get("payload")).isNotNull();
        JsonObject payload = tested.content.get("payload").getAsJsonObject();

        Assertions.assertThat(payload.get("string").getAsString()).isEqualTo("string");
        Assertions.assertThat(payload.get("integer").getAsInt()).isEqualTo(123456);
        Assertions.assertThat(payload.get("double").getAsDouble()).isEqualTo(1.2345);
        Assertions.assertThat(payload.get("long").getAsLong()).isEqualTo(9223372036854775806L);
        Assertions.assertThat(payload.get("longWithE").getAsLong()).isEqualTo(9223372036000000000L);
        Assertions.assertThat(payload.get("doubleWithE").getAsDouble()).isEqualTo(0.00014);

        Assertions.assertThat(payload.get("true").getAsBoolean()).isTrue();
        Assertions.assertThat(payload.get("false").getAsBoolean()).isFalse();

        Assertions.assertThat(payload.get("null").isJsonNull());
        Assertions.assertThat(payload.get("object").getAsJsonObject().get("foo").getAsString()).isEqualTo("bar");
        Assertions.assertThat(payload.get("array").getAsJsonArray().size()).isEqualTo(5);
    }

    @Test
    public void test_should_be_parcelable_as_a_list() throws Exception {
        ResolveAction[] tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_002,
                InstrumentationRegistry.getContext()), ResolveAction[].class);

        Assertions.assertThat(tested).hasSize(2);

        Assertions.assertThat(tested[0].content.get("url")).isNotNull();
        Assertions.assertThat(tested[1].content.get("url")).isNotNull();
    }
}
