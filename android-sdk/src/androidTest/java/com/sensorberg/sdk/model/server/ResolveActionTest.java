package com.sensorberg.sdk.model.server;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import javax.inject.Inject;

import util.Utils;

import static util.Conditions.size;

public class ResolveActionTest extends SensorbergApplicationTest {

    @Inject
    Gson gson;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    public void test_should_be_parseable() throws Exception {
        ResolveAction tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_001, getContext()), ResolveAction.class);

        Assertions.assertThat(tested.content.getString("url")).isNotNull();

        Assertions.assertThat(tested.content.getJSONObject("payload").getString("string")).isEqualTo("string");
        Assertions.assertThat(tested.content.getJSONObject("payload").getInt("integer")).isEqualTo(123456);
        Assertions.assertThat(tested.content.getJSONObject("payload").getDouble("double")).isEqualTo(1.2345);
        Assertions.assertThat(tested.content.getJSONObject("payload").getLong("long")).isEqualTo(9223372036854775806L);
        Assertions.assertThat(tested.content.getJSONObject("payload").getLong("longWithE")).isEqualTo(9223372036000000000L);
        Assertions.assertThat(tested.content.getJSONObject("payload").getDouble("doubleWithE")).isEqualTo(0.00014);

        Assertions.assertThat(tested.content.getJSONObject("payload").getBoolean("true")).isTrue();
        Assertions.assertThat(tested.content.getJSONObject("payload").getBoolean("false")).isFalse();

        Assertions.assertThat(tested.content.getJSONObject("payload").get("null")).isEqualTo(JSONObject.NULL);
        Assertions.assertThat(tested.content.getJSONObject("payload").getJSONObject("object").getString("foo")).isEqualTo("bar");
        Assertions.assertThat(tested.content.getJSONObject("payload").getJSONArray("array")).has(size(5));
    }


    public void test_should_be_parcelable_as_a_list() throws Exception {

        ResolveAction[] tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_002, getContext()), ResolveAction[].class);

        Assertions.assertThat(tested).hasSize(2);

        Assertions.assertThat(tested[0].content.getString("url")).isNotNull();
        Assertions.assertThat(tested[1].content.getString("url")).isNotNull();
    }



}
