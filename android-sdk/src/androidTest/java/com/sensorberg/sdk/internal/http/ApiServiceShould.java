package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.testUtils.SuccessfulRetrofitApiService;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(AndroidJUnit4.class)
public class ApiServiceShould {

    @Inject
    Gson gson;

    @Inject
    Context mContext;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier realPlatformIdentifier;

    @Inject
    @Named("realRetrofitApiService")
    RetrofitApiServiceImpl realRetrofitApiService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Test
    public void apiservice_should_have_valid_useragent_in_header() throws Exception {
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_USER_AGENT))
                .isEqualTo(realPlatformIdentifier.getUserAgentString());
    }

    @Test
    public void apiservice_should_have_advertiserid_in_header() throws Exception {
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Thread.sleep(2000); //we wait for google to give us the advertiser id //TODO this will change!

        Assertions.assertThat(realPlatformIdentifier.getAdvertiserIdentifier())
                .isNotNull();
        Response<BaseResolveResponse> responseWithAdvertiserId = call.clone().execute();

        Assertions.assertThat(responseWithAdvertiserId.raw().request().headers().get(Transport.HEADER_ADVERTISER_IDENTIFIER))
                .isEqualTo(realPlatformIdentifier.getAdvertiserIdentifier());
    }

    @Test
    public void apiservice_should_have_valid_installationid_in_header() throws Exception {
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_INSTALLATION_IDENTIFIER))
                .isEqualTo(realPlatformIdentifier.getDeviceInstallationIdentifier());

    }

    @Test
    public void apiservice_should_have_apitoken_header() throws Exception {
        final String API_TOKEN = "test_api_token";
        realRetrofitApiService.setApiToken(API_TOKEN);
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_XAPIKEY))
                .isEqualTo(API_TOKEN);
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_AUTHORIZATION))
                .isEqualTo(API_TOKEN);
    }

    @Test
    public void apiservice_should_throw_an_unknown_host_exception() throws Exception {
        SuccessfulRetrofitApiService retrofitApiService = new SuccessfulRetrofitApiService(mContext, gson, realPlatformIdentifier,
                "http://localhost/");
        retrofitApiService.getOriginalOkHttpClient().cache().evictAll();

        exception.expect(UnknownHostException.class);

        OkHttpClient client = retrofitApiService.getOriginalOkHttpClient();
        okhttp3.Response okHttpResponse = client.newCall(new Request.Builder().url("https://test.comxxx").get().build())
                .execute();
        Assertions.assertThat(okHttpResponse).isNotNull();
    }

    @Test
    public void apiservice_should_cache_responses() throws Exception {
        Call<BaseResolveResponse> call1 = realRetrofitApiService.updateBeaconLayout("");

        Response<BaseResolveResponse> response1 = call1.execute();
        Assertions.assertThat(response1.isSuccessful()).isTrue();

        Call<BaseResolveResponse> call2 = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response2 = call2.execute();
        Assertions.assertThat(response2.isSuccessful()).isTrue();

        Assertions.assertThat(response2.raw().cacheResponse()).isNotNull();
        Assertions.assertThat(response2.raw().networkResponse()).isNull();
    }

}
