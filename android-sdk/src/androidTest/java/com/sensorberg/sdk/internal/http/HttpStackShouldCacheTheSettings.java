package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Response;
import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class HttpStackShouldCacheTheSettings {

    @Inject
    Gson gson;

    @Inject
    @Named("realRetrofitApiService")
    RetrofitApiServiceImpl realRetrofitApiService;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        realRetrofitApiService.setApiToken(TestConstants.API_TOKEN);
    }

    @Test
    public void test_should_only_call_the_network_once() throws Exception {
        Call<SettingsResponse> call = realRetrofitApiService.getSettings();

        Response response1 = call.execute();
        Response response2 = call.clone().execute();

        Assertions.assertThat(response1).isNotNull();
        Assertions.assertThat(response1.isSuccessful()).isTrue();
        Assertions.assertThat(response2).isNotNull();
        Assertions.assertThat(response2.isSuccessful()).isTrue();
        Assertions.assertThat(response2.raw().cacheResponse()).isNotNull();
    }
}
