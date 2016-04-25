package com.sensorberg.sdk.model.server;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import java.io.IOException;

import javax.inject.Inject;

import util.Utils;

public class TheResolveResponse extends SensorbergApplicationTest {

    @Inject
    Gson gson;

    @Override
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Test
    public void should_parse_response_from_the_resolver() throws IOException {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.response_layout_parse_error, getContext()), ResolveResponse.class);
        Assertions.assertThat(tested).isNotNull();
    }
}
