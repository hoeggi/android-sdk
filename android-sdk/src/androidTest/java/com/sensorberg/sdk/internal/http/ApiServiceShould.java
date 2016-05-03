package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;

import org.fest.assertions.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(AndroidJUnit4.class)
public class ApiServiceShould {

    @Inject
    Gson gson;

    @Inject
    @Named("realRetrofitApiService")
    RetrofitApiServiceImpl realRetrofitApiService;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Test
    public void apiservice_should_have_valid_useragent_in_header() throws Exception {
        final MockWebServer server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}"));

        Call<SettingsResponse> call = realRetrofitApiService.getSettings();

        call.enqueue(new Callback<SettingsResponse>() {
            @Override
            public void onResponse(Call<SettingsResponse> call, Response<SettingsResponse> response) {
                Assertions.assertThat(response.isSuccessful()).isTrue();
                Assertions.assertThat(response.headers().get(Transport.HEADER_USER_AGENT)).isEqualTo(testPlatformIdentifier.getUserAgentString());
            }

            @Override
            public void onFailure(Call<SettingsResponse> call, Throwable t) {
                Assert.fail();
            }
        });
    }

    @Test
    public void apiservice_should_have_advertiserid_in_header() throws Exception {
        final MockWebServer server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}"));

        Call<SettingsResponse> call = realRetrofitApiService.getSettings();

        call.enqueue(new Callback<SettingsResponse>() {
            @Override
            public void onResponse(Call<SettingsResponse> call, Response<SettingsResponse> response) {
                Assertions.assertThat(response.isSuccessful()).isTrue();
                Assertions.assertThat(response.headers().get(Transport.HEADER_ADVERTISER_IDENTIFIER))
                        .isEqualTo(testPlatformIdentifier.getAdvertiserIdentifier());
            }

            @Override
            public void onFailure(Call<SettingsResponse> call, Throwable t) {
                Assert.fail();
            }
        });
    }

    @Test
    public void apiservice_should_have_valid_installationid_in_header() throws Exception {
        final MockWebServer server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}"));

        Call<SettingsResponse> call = realRetrofitApiService.getSettings();

        call.enqueue(new Callback<SettingsResponse>() {
            @Override
            public void onResponse(Call<SettingsResponse> call, Response<SettingsResponse> response) {
                Assertions.assertThat(response.isSuccessful()).isTrue();
                Assertions.assertThat(response.headers().get(Transport.HEADER_INSTALLATION_IDENTIFIER))
                        .isEqualTo(testPlatformIdentifier.getDeviceInstallationIdentifier());
            }

            @Override
            public void onFailure(Call<SettingsResponse> call, Throwable t) {
                Assert.fail();
            }
        });
    }

}
